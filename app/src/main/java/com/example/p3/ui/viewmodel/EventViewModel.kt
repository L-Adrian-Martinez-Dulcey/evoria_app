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
import com.example.p3.data.repository.EventRepository
import com.example.p3.data.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    init { loadEvents() }

    fun loadEvents() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val events = repository.getEvents()
            _uiState.value = _uiState.value.copy(events = events, isLoading = false)
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

    fun save(event: Event, selectedImageUri: Uri? = null, onSuccess: () -> Unit) = viewModelScope.launch {
        if (_uiState.value.isLoading) return@launch
        val validation = validate(event)
        if (validation != null) { _uiState.value = _uiState.value.copy(error = validation); return@launch }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
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

    fun delete(event: Event, onSuccess: () -> Unit) = viewModelScope.launch {
        val id = requireNotNull(event.id)
        _uiState.value = _uiState.value.copy(events = _uiState.value.events - event, message = "Evento eliminado")
        runCatching { repository.delete(id) }
            .onSuccess { onSuccess() }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    events = _uiState.value.events + event,
                    error = "No fue posible eliminar: ${it.message}",
                )
            }
    }

    fun register(event: Event, userId: String) = viewModelScope.launch {
        when {
            event.creatorId == userId -> fail("No puedes inscribirte a tu propio evento.")
            event.availableSlots <= 0 -> fail("No hay cupos disponibles.")
            event.registrations.any { it.userId == userId } -> fail("Ya estás inscrito en este evento.")
            else -> updateEvent(event.copy(
                availableSlots = event.availableSlots - 1,
                registrations = event.registrations + Registration(
                    id = UUID.randomUUID().toString(), eventId = event.id.orEmpty(), userId = userId,
                    registrationDate = now(),
                )
            ), "Inscripción realizada")
        }
    }

    fun addReview(event: Event, userId: String, rating: Int, comment: String) {
        if (!isFinished(event.date)) return fail("Solo puedes calificar eventos finalizados.")
        if (rating !in 1..5 || comment.isBlank()) return fail("Indica una calificación de 1 a 5 y un comentario.")
        if (event.reviews.any { it.userId == userId }) return fail("Ya calificaste este evento.")
        updateEvent(event.copy(reviews = event.reviews + Review(UUID.randomUUID().toString(), event.id.orEmpty(), userId, rating, comment.trim())), "Reseña publicada")
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(error = null, message = null) }

    private fun updateEvent(event: Event, success: String) = viewModelScope.launch {
        val previous = _uiState.value.events.firstOrNull { it.id == event.id }
        _uiState.value = _uiState.value.copy(
            events = _uiState.value.events.map { if (it.id == event.id) event else it },
            message = success,
        )
        runCatching { repository.update(event) }
            .onSuccess { updated ->
                _uiState.value = _uiState.value.copy(
                    events = _uiState.value.events.map { if (it.id == updated.id) updated else it },
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    events = previous?.let { old ->
                        _uiState.value.events.map { if (it.id == old.id) old else it }
                    } ?: _uiState.value.events,
                    error = "No fue posible actualizar el evento: ${it.message}",
                )
            }
    }

    private fun validate(event: Event): String? = when {
        listOf(event.title, event.description, event.date, event.time, event.place, event.category).any { it.isBlank() } -> "Completa todos los campos obligatorios."
        event.availableSlots < 0 -> "Los cupos no pueden ser negativos."
        !isValidFutureDate(event.date) -> "La fecha debe ser desde mañana y tener formato AAAA-MM-DD."
        else -> null
    }
    private fun isValidFutureDate(value: String): Boolean = runCatching {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val selected = format.parse(value) ?: return false
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
        !selected.before(tomorrow)
    }.getOrDefault(false)
    private fun isFinished(value: String): Boolean = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value)?.before(Calendar.getInstance().time) == true }.getOrDefault(false)
    private fun now() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Calendar.getInstance().time)
    private fun fail(message: String) { _uiState.value = _uiState.value.copy(error = message) }

}
