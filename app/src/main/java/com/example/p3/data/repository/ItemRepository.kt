package com.example.p3.data.repository

import com.example.p3.data.api.ApiService
import com.example.p3.data.model.Item

class ItemRepository(private val apiService: ApiService) {
    suspend fun getItems() = apiService.getItems()
    suspend fun getItem(id: String) = apiService.getItem(id)
    suspend fun createItem(item: Item) = apiService.createItem(item)
    suspend fun updateItem(id: String, item: Item) = apiService.updateItem(id, item)
    suspend fun deleteItem(id: String) = apiService.deleteItem(id)
}