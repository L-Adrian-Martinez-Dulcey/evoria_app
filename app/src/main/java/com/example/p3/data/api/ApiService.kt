package com.example.p3.data.api

import com.example.p3.data.model.Event
import com.example.p3.data.model.User
import retrofit2.http.*

interface ApiService {
    @GET("Evento")
    suspend fun getEvents(): List<Event>

    @GET("Evento/{id}")
    suspend fun getEvent(@Path("id") id: String): Event

    @POST("Evento")
    suspend fun createEvent(@Body event: Event): Event

    @PUT("Evento/{id}")
    suspend fun updateEvent(@Path("id") id: String, @Body event: Event): Event

    @DELETE("Evento/{id}")
    suspend fun deleteEvent(@Path("id") id: String)

    @GET("user")
    suspend fun getUserByEmail(@Query("email") email: String): List<User>

    // User CRUD
    @GET("user")
    suspend fun getUsers(): List<User>

    @GET("user/{id}")
    suspend fun getUser(@Path("id") id: String): User

    @POST("user")
    suspend fun createUser(@Body user: User): User

    @PUT("user/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: User): User

    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") id: String)
}
