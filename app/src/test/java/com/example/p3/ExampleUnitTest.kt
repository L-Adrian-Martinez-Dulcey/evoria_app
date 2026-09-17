package com.example.p3

import com.example.p3.data.model.Event
import com.example.p3.data.model.Registration
import com.example.p3.data.validation.EventValidator
import org.junit.Test
import java.util.Calendar

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun eventValidator_rejects_capacity_below_current_registrations() {
        val event = validEvent().copy(
            availableSlots = 1,
            registrations = listOf(
                Registration(userId = "user-1"),
                Registration(userId = "user-2"),
            ),
        )

        assertEquals(
            "Los cupos no pueden ser menores que las inscripciones actuales.",
            EventValidator.validate(event, fixedToday()),
        )
    }

    @Test
    fun eventValidator_accepts_event_from_tomorrow() {
        val tomorrow = (fixedToday().clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val event = validEvent().copy(
            date = "%04d-%02d-%02d".format(
                tomorrow.get(Calendar.YEAR),
                tomorrow.get(Calendar.MONTH) + 1,
                tomorrow.get(Calendar.DAY_OF_MONTH),
            ),
        )

        assertNull(EventValidator.validate(event, fixedToday()))
    }

    @Test
    fun eventValidator_rejects_missing_required_fields() {
        assertEquals(
            "Completa todos los campos obligatorios.",
            EventValidator.validate(validEvent().copy(title = ""), fixedToday()),
        )
    }

    private fun validEvent() = Event(
        title = "Evento de prueba",
        description = "Descripción",
        date = "2026-09-18",
        time = "18:00",
        place = "Auditorio",
        category = "Tecnología",
        availableSlots = 10,
    )

    private fun fixedToday() = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2026)
        set(Calendar.MONTH, Calendar.SEPTEMBER)
        set(Calendar.DAY_OF_MONTH, 16)
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}