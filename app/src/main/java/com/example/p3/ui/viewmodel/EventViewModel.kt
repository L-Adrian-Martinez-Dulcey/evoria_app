package com.example.p3.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.p3.data.api.GithubRetrofitClient
import com.example.p3.data.api.RetrofitClient
import com.example.p3.data.model.Event
import com.example.p3.data.model.Registration
import com.example.p3.data.model.Review
import com.example.p3.data.model.AppNotification
import com.example.p3.data.repository.EventRepository
import com.example.p3.data.repository.ImageRepository
import com.example.p3.data.validation.EventValidator
import com.example.p3.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class EventUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EventRepository(RetrofitClient.apiService)
    private val imageRepository = ImageRepository(
        application.contentResolver,
        GithubRetrofitClient.apiService,
    )
    private val sessionManager = SessionManager(application)
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()
    private val registrationMutationMutex = Mutex()

    init { loadEvents() }

    fun loadEvents() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val events = repository.getEvents()
            _uiState.value = _uiState.value.copy(events = events, isLoading = false)
            sessionManager.getUserId()?.let { syncNotifications(it, events) }
        } catch (error: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "No fue posible cargar eventos: ${error.message}",
            )
        }
    }

    fun loadEvent(id: String) = viewModelScope.launch {
        if (_uiState.value.events.any { it.id == id }) return@launch
        try {
            val restored = repository.getEvent(id)
            _uiState.value = _uiState.value.copy(
                events = _uiState.value.events
                    .filterNot { it.id == restored.id }
                    .plus(restored),
                error = null,
            )
        } catch (error: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "No fue posible cargar el evento: ${error.message}",
            )
        }
    }

    fun save(
        event: Event,
        userId: String,
        selectedImageUri: Uri? = null,
        onSuccess: () -> Unit,
    ) {
        // El estado debe cambiar antes de iniciar la corrutina. De otro modo dos
        // pulsaciones rápidas pueden encolar dos POST antes de que la primera
        // corrutina alcance su comprobación de isLoading.
        if (_uiState.value.isLoading) return
        if (event.id != null) {
            val currentEvent = _uiState.value.events.firstOrNull { it.id == event.id }
            if (currentEvent == null) {
                fail("No fue posible verificar el propietario del evento.")
                return
            }
            if (currentEvent.creatorId != userId) {
                fail("Solo el creador puede editar este evento.")
                return
            }
        }
        val validation = EventValidator.validate(event)
        if (validation != null) {
            _uiState.value = _uiState.value.copy(error = validation)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val saved = if (event.id == null) {
                    val created = repository.create(event.copy(coverImage = ""))
                    val imageUrl = selectedImageUri?.let {
                        imageRepository.uploadEventImage(it, requireNotNull(created.id))
                    }
                    if (imageUrl != null) {
                        repository.update(created.copy(coverImage = imageUrl))
                    } else {
                        created
                    }
                } else {
                    val imageUrl = selectedImageUri?.let {
                        imageRepository.uploadEventImage(it, requireNotNull(event.id))
                    }
                    repository.update(event.copy(coverImage = imageUrl ?: event.coverImage))
                }
                val currentEvents = _uiState.value.events
                val updatedEvents = if (event.id == null) {
                    currentEvents + saved
                } else {
                    currentEvents.map { if (it.id == saved.id) saved else it }
                }
                _uiState.value = _uiState.value.copy(events = updatedEvents)
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Evento guardado")
                onSuccess()
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No fue posible guardar: ${error.message}",
                )
            }
        }
    }

    fun delete(event: Event, userId: String, onSuccess: () -> Unit) = viewModelScope.launch {
        val id = requireNotNull(event.id)
        if (event.creatorId != userId) {
            fail("Solo el creador puede eliminar este evento.")
            return@launch
        }
        if (_uiState.value.isLoading) return@launch
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        _uiState.value = _uiState.value.copy(events = _uiState.value.events - event, message = "Evento eliminado")
        runCatching { repository.delete(id) }
            .onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    events = _uiState.value.events + event,
                    isLoading = false,
                    error = "No fue posible eliminar: ${it.message}",
                )
            }
    }

    fun register(event: Event, userId: String, userName: String = "Un usuario") = viewModelScope.launch {
        val eventId = event.id ?: return@launch
        registrationMutationMutex.withLock {
            if (_uiState.value.isLoading) return@withLock
            mutateRegistration(
                eventId = eventId,
                userId = userId,
                userName = userName,
                successMessage = "Inscripción realizada",
                notifyCreator = true,
            ) { latest ->
                when {
                    latest.creatorId == userId ->
                        RegistrationMutation.Rejected("No puedes inscribirte a tu propio evento.")
                    latest.hasEnded() ->
                        RegistrationMutation.Rejected("No puedes inscribirte a un evento finalizado.")
                    latest.availableSlots <= 0 ->
                        RegistrationMutation.Rejected("No hay cupos disponibles.")
                    latest.registrations.any { it.userId == userId } ->
                        RegistrationMutation.Rejected("Ya estás inscrito en este evento.")
                    else ->
                        RegistrationMutation.Updated(
                            latest.copy(
                                availableSlots = latest.availableSlots - 1,
                                registrations = latest.registrations + Registration(
                                    id = UUID.randomUUID().toString(),
                                    eventId = eventId,
                                    userId = userId,
                                    registrationDate = now(),
                                )
                            )
                        )
                }
            }
        }
    }

    fun unregister(event: Event, userId: String) = viewModelScope.launch {
        val eventId = event.id ?: return@launch
        registrationMutationMutex.withLock {
            if (_uiState.value.isLoading) return@withLock
            mutateRegistration(
                eventId = eventId,
                userId = userId,
                userName = "Usuario",
                successMessage = "Inscripción cancelada correctamente.",
                notifyCreator = false,
            ) { latest ->
                if (latest.registrations.none { it.userId == userId }) {
                    RegistrationMutation.Rejected("No estás inscrito en este evento.")
                } else {
                    RegistrationMutation.Updated(
                        latest.copy(
                            availableSlots = latest.availableSlots + 1,
                            registrations = latest.registrations.filterNot { it.userId == userId },
                        )
                    )
                }
            }
        }
    }

    private suspend fun mutateRegistration(
        eventId: String,
        userId: String,
        userName: String,
        successMessage: String,
        notifyCreator: Boolean,
        mutation: (Event) -> RegistrationMutation,
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
        try {
            val latest = repository.getEvent(eventId)
            when (val result = mutation(latest)) {
                is RegistrationMutation.Rejected -> {
                    replaceEvent(latest)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
                is RegistrationMutation.Updated -> {
                    val updated = repository.update(result.event)
                    replaceEvent(updated)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = successMessage,
                    )
                    if (notifyCreator && updated.creatorId.isNotBlank() && updated.creatorId != userId) {
                        sessionManager.addNotificationIfAbsent(
                            AppNotification(
                                id = UUID.randomUUID().toString(),
                                recipientId = updated.creatorId,
                                title = "Nueva inscripción",
                                message = "$userName se inscribió a tu evento \"${updated.title}\".",
                                eventId = eventId,
                                createdAt = now(),
                            )
                        )
                    }
                }
            }
        } catch (error: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "No fue posible actualizar la inscripción: ${error.message}",
            )
        }
    }

    private fun replaceEvent(event: Event) {
        _uiState.value = _uiState.value.copy(
            events = _uiState.value.events
                .filterNot { it.id == event.id }
                .plus(event),
        )
    }

    private sealed interface RegistrationMutation {
        data class Updated(val event: Event) : RegistrationMutation
        data class Rejected(val message: String) : RegistrationMutation
    }

    fun addReview(
        event: Event,
        userId: String,
        userName: String = "Un usuario",
        rating: Int,
        comment: String,
    ) {
        val eventId = event.id ?: return fail("No fue posible identificar el evento.")
        viewModelScope.launch {
            if (_uiState.value.isLoading) return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val latest = repository.getEvent(eventId)
                when {
                    !latest.hasEnded() -> fail("Solo puedes calificar eventos finalizados.")
                    latest.registrations.none { it.userId == userId } ->
                        fail("Solo los asistentes inscritos pueden calificar el evento.")
                    rating !in 1..5 || comment.trim().isBlank() ->
                        fail("Indica una calificación de 1 a 5 y un comentario.")
                    comment.trim().length > 500 ->
                        fail("El comentario no puede superar 500 caracteres.")
                    latest.reviews.any { it.userId == userId } ->
                        fail("Ya calificaste este evento.")
                    else -> {
                        val updated = repository.update(
                            latest.copy(
                                reviews = latest.reviews + Review(
                                    UUID.randomUUID().toString(),
                                    eventId,
                                    userId,
                                    rating,
                                    comment.trim(),
                                ),
                            ),
                        )
                        replaceEvent(updated)
                        _uiState.value = _uiState.value.copy(message = "Reseña publicada")
                        if (updated.creatorId.isNotBlank() && updated.creatorId != userId) {
                            sessionManager.addNotificationIfAbsent(
                                AppNotification(
                                    id = "review:$eventId:${updated.reviews.last().id}",
                                    recipientId = updated.creatorId,
                                    title = "Nueva reseña",
                                    message = "$userName dejó una reseña de ${updated.title}.",
                                    eventId = eventId,
                                    createdAt = now(),
                                ),
                            )
                        }
                    }

                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "No fue posible publicar la reseña: ${error.message}",
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun syncNotifications(userId: String, events: List<Event> = _uiState.value.events) {
        viewModelScope.launch {
            events.forEach { event ->
                val eventId = event.id ?: return@forEach
                if (event.creatorId == userId) {
                    event.registrations.forEach { registration ->
                        sessionManager.addNotificationIfAbsent(
                            AppNotification(
                                id = "registration:$eventId:${registration.id}",
                                recipientId = userId,
                                title = "Nueva inscripción",
                                message = "Un usuario se inscribió a tu evento \"${event.title}\".",
                                eventId = eventId,
                                createdAt = registration.registrationDate.ifBlank { now() },
                            ),
                        )
                    }
                    event.reviews.forEach { review ->
                        sessionManager.addNotificationIfAbsent(
                            AppNotification(
                                id = "review:$eventId:${review.id}",
                                recipientId = userId,
                                title = "Nueva reseña",
                                message = "Un usuario dejó una reseña de ${event.title}.",
                                eventId = eventId,
                                createdAt = now(),
                            ),
                        )
                    }
                }
                if (event.registrations.any { it.userId == userId } && event.startsWithinNextHour()) {
                    sessionManager.addNotificationIfAbsent(
                        AppNotification(
                            id = "reminder:$eventId:${event.date}_${event.time}",
                            recipientId = userId,
                            title = "Evento próximo",
                            message = "Falta menos de una hora para que comience \"${event.title}\".",
                            eventId = eventId,
                            createdAt = now(),
                        ),
                    )
                }
            }
        }
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(error = null, message = null) }

    private fun updateEvent(event: Event, success: String) = viewModelScope.launch {
        if (_uiState.value.isLoading) return@launch
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        val previous = _uiState.value.events.firstOrNull { it.id == event.id }
        _uiState.value = _uiState.value.copy(
            events = _uiState.value.events.map { if (it.id == event.id) event else it },
            message = success,
        )
        runCatching { repository.update(event) }
            .onSuccess { updated ->
                _uiState.value = _uiState.value.copy(
                    events = _uiState.value.events.map { if (it.id == updated.id) updated else it },
                    isLoading = false,
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    events = previous?.let { old ->
                        _uiState.value.events.map { if (it.id == old.id) old else it }
                    } ?: _uiState.value.events,
                    isLoading = false,
                    error = "No fue posible actualizar el evento: ${it.message}",
                )
            }
    }

    private fun now() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Calendar.getInstance().time)
    private fun fail(message: String) { _uiState.value = _uiState.value.copy(error = message) }

}
