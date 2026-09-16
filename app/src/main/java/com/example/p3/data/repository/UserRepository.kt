package com.example.p3.data.repository

import com.example.p3.data.api.ApiService
import com.example.p3.data.model.User
import retrofit2.HttpException

class UserRepository(private val apiService: ApiService) {
    suspend fun getUsers() = apiService.getUsers()
    suspend fun getUser(id: String) = apiService.getUser(id)
    suspend fun createUser(user: User) = apiService.createUser(user)
    suspend fun updateUser(id: String, user: User) = apiService.updateUser(id, user)

    suspend fun getUserByEmail(email: String): List<User> =
        try {
            apiService.getUserByEmail(email)
        } catch (error: HttpException) {
            if (error.code() == 404) emptyList() else throw error
        }

    suspend fun deleteUser(id: String) = apiService.deleteUser(id)
}