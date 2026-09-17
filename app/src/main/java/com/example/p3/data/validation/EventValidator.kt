package com.example.p3.data.validation

import com.example.p3.data.model.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object EventValidator {
    fun validate(event: Event, today: Calendar = Calendar.getInstance()): String? = when {
        listOf(
            event.title,
            event.description,
            event.date,
            event.time,
            event.place,
            event.category,
        ).any { it.isBlank() } -> "Completa todos los campos obligatorios."
        event.availableSlots < 0 -> "Los cupos no pueden ser negativos."
        event.availableSlots < event.registrations.size ->
            "Los cupos no pueden ser menores que las inscripciones actuales."
        !isValidFutureDate(event.date, today) ->
            "La fecha debe ser desde mañana y tener formato AAAA-MM-DD."
        else -> null
    }

    fun isValidFutureDate(value: String, today: Calendar = Calendar.getInstance()): Boolean = runCatching {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val selected = format.parse(value) ?: return false
        val tomorrow = (today.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        !selected.before(tomorrow)
    }.getOrDefault(false)
}
