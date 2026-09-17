package com.example.p3.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** El recurso remoto sigue llamándose `item`, pero en la app representa un evento. */
data class Event(
    @SerializedName("id") val id: String? = null,
    @SerializedName("creatorId") val creatorId: String = "",
    @SerializedName("name") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("date") val date: String = "",
    @SerializedName("time") val time: String = "",
    @SerializedName("place") val place: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("availableSlots") val availableSlots: Int = 0,
    @SerializedName("avatar") val coverImage: String = "",
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("registrations") val registrations: List<Registration> = emptyList(),
    @SerializedName("reviews") val reviews: List<Review> = emptyList(),
) {
    fun hasEnded(now: Date = Calendar.getInstance().time): Boolean = runCatching {
        startsAt()?.before(now) == true
    }.getOrDefault(false)

    fun startsAt(): Date? {
        if (date.isBlank() || time.isBlank()) return null
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            isLenient = false
        }
        val value = "$date $time"
        val position = ParsePosition(0)
        return format.parse(value, position)
            ?.takeIf { position.index == value.length }
    }

    fun startsWithinNextHour(now: Date = Calendar.getInstance().time): Boolean {
        val start = startsAt() ?: return false
        val difference = start.time - now.time
        return difference > 0 && difference <= 60 * 60 * 1000L
    }
}

data class Registration(
    @SerializedName("id") val id: String = "",
    @SerializedName("eventId") val eventId: String = "",
    @SerializedName("userId") val userId: String = "",
    @SerializedName("registrationDate") val registrationDate: String = "",
)

data class Review(
    @SerializedName("id") val id: String = "",
    @SerializedName("eventId") val eventId: String = "",
    @SerializedName("userId") val userId: String = "",
    @SerializedName("rating") val rating: Int = 0,
    @SerializedName("comment") val comment: String = "",
)
