package com.example.p3.data.repository

import com.example.p3.data.api.ApiService
import com.example.p3.data.model.Event

class EventRepository(private val api: ApiService) {
    suspend fun getEvents() = api.getEvents()
    suspend fun getEvent(id: String) = api.getEvent(id)
    suspend fun create(event: Event) = api.createEvent(event)
    suspend fun update(event: Event) = api.updateEvent(requireNotNull(event.id), event)
    suspend fun delete(id: String) = api.deleteEvent(id)
}
