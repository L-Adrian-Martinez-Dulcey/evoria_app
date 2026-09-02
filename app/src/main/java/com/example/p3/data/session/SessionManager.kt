package com.example.p3.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.sessionDataStore by preferencesDataStore(name = "evoria_session")

class SessionManager(private val context: Context) {
    private val userIdKey = stringPreferencesKey("user_id")

    suspend fun saveUserId(id: String) {
        context.sessionDataStore.edit { it[userIdKey] = id }
    }

    suspend fun getUserId(): String? = context.sessionDataStore.data.first()[userIdKey]

    suspend fun clear() {
        context.sessionDataStore.edit { it.remove(userIdKey) }
    }
}
