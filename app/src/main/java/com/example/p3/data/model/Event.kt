package com.example.p3.data.model

import com.google.gson.annotations.SerializedName

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
)

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
