package com.example.p3.data.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "evoria_session")

class SessionManager(private val context: Context) {
    private val userIdKey = stringPreferencesKey("user_id")
    private val onboardingKey = booleanPreferencesKey("onboarding_completed")

    suspend fun saveUserId(id: String) {
        context.sessionDataStore.edit { it[userIdKey] = id }
    }

    suspend fun getUserId(): String? = context.sessionDataStore.data.first()[userIdKey]

    suspend fun saveOnboardingCompleted() {
        context.sessionDataStore.edit { it[onboardingKey] = true }
    }

    val isOnboardingCompleted: Flow<Boolean> = context.sessionDataStore.data.map {
        it[onboardingKey] ?: false
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.remove(userIdKey) }
    }
}
