package com.example.p3

import com.example.p3.data.model.Event
import java.text.SimpleDateFormat
import java.util.Locale
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventModelTest {
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    @Test
    fun hasEndedRequiresTheCompleteDateAndTime() {
        val event = Event(date = "2026-09-16", time = "09:00")

        assertTrue(event.hasEnded(format.parse("2026-09-16 10:00")!!))
        assertFalse(event.hasEnded(format.parse("2026-09-16 08:00")!!))
    }

    @Test
    fun invalidDateOrTimeIsNotConsideredEnded() {
        val event = Event(date = "2026-99-99", time = "invalid")

        assertFalse(event.hasEnded(format.parse("2026-09-16 10:00")!!))
    }
}
