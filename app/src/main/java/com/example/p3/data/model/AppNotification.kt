package com.example.p3.data.model

data class AppNotification(
    val id: String,
    val recipientId: String,
    val title: String,
    val message: String,
    val eventId: String,
    val createdAt: String,
    val read: Boolean = false,
)
