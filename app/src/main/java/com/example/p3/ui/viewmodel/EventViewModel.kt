package com.example.p3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p3.data.api.RetrofitClient
import com.example.p3.data.model.Event
import com.example.p3.data.model.Registration
import com.example.p3.data.model.Review
import com.example.p3.data.repository.EventRepository
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

class EventViewModel : ViewModel() {
    private val repository = EventRepository(RetrofitClient.apiService)
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    init { loadEvents() }

    fun loadEvents() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching { repository.getEvents() }
            .onSuccess { _uiState.value = _uiState.value.copy(events = it, isLoading = false) }
            .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "No fue posible cargar eventos: ${it.message}") }
    }

    fun loadEvent(id: String) = viewModelScope.launch {
        runCatching { repository.getEvent(id) }
            .onSuccess { event ->
                _uiState.value = _uiState.value.copy(
                    events = _uiState.value.events
                        .filterNot { it.id == event.id }
                        .plus(event),
                    error = null,
                )
            }
            .onFailure { _uiState.value = _uiState.value.copy(error = "No fue posible cargar el evento: ${it.message}") }
    }

    fun save(event: Event, onSuccess: () -> Unit) = viewModelScope.launch {
        val validation = validate(event)
        if (validation != null) { _uiState.value = _uiState.value.copy(error = validation); return@launch }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching { if (event.id == null) repository.create(event) else repository.update(event) }
            .onSuccess { loadEvents(); _uiState.value = _uiState.value.copy(isLoading = false, message = "Evento guardado"); onSuccess() }
            .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "No fue posible guardar: ${it.message}") }
    }

    fun delete(event: Event, onSuccess: () -> Unit) = viewModelScope.launch {
        runCatching { repository.delete(requireNotNull(event.id)) }
            .onSuccess { _uiState.value = _uiState.value.copy(events = _uiState.value.events - event, message = "Evento eliminado"); onSuccess() }
            .onFailure { _uiState.value = _uiState.value.copy(error = "No fue posible eliminar: ${it.message}") }
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
        runCatching { repository.update(event) }
            .onSuccess { updated -> _uiState.value = _uiState.value.copy(events = _uiState.value.events.map { if (it.id == updated.id) updated else it }, message = success) }
            .onFailure { _uiState.value = _uiState.value.copy(error = "No fue posible actualizar el evento: ${it.message}") }
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
