package com.example.p3.data.api

import com.example.p3.data.model.Item
import com.example.p3.data.model.Event
import com.example.p3.data.model.User
import retrofit2.http.*

interface ApiService {
    // Evoria: MockAPI solo dispone del recurso /item; se utiliza como /events.
    @GET("item")
    suspend fun getEvents(): List<Event>

    @GET("item/{id}")
    suspend fun getEvent(@Path("id") id: String): Event

    @POST("item")
    suspend fun createEvent(@Body event: Event): Event

    @PUT("item/{id}")
    suspend fun updateEvent(@Path("id") id: String, @Body event: Event): Event

    @DELETE("item/{id}")
    suspend fun deleteEvent(@Path("id") id: String)

    // Item CRUD
    @GET("item")
    suspend fun getItems(): List<Item>

    @GET("item/{id}")
    suspend fun getItem(@Path("id") id: String): Item

    @GET("user")
    suspend fun getUserByEmail(@Query("email") email: String): List<User>

    @POST("item")
    suspend fun createItem(@Body item: Item): Item

    @PUT("item/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: Item): Item

    @DELETE("item/{id}")
    suspend fun deleteItem(@Path("id") id: String)

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
