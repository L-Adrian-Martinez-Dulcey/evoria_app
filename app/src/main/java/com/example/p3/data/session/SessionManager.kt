package com.example.p3.data.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.p3.data.model.AppNotification
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Almacena la sesión del usuario en DataStore para persistir datos locales del app.
private val Context.sessionDataStore by preferencesDataStore(name = "evoria_session")

class SessionManager(private val context: Context) {

    // Claves para guardar información básica de la sesión y preferencias del usuario.
    private val userIdKey = stringPreferencesKey("user_id")
    private val onboardingKey = booleanPreferencesKey("onboarding_completed")
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val notificationsKey = stringPreferencesKey("notifications")
    private val gson = Gson()

    suspend fun saveUserId(id: String) {
        context.sessionDataStore.edit {
            it[userIdKey] = id
        }
    }

    suspend fun getUserId(): String? =
        context.sessionDataStore.data.first()[userIdKey]

    suspend fun saveOnboardingCompleted() {
        context.sessionDataStore.edit {
            it[onboardingKey] = true
        }
    }

    val isOnboardingCompleted: Flow<Boolean> =
        context.sessionDataStore.data.map {
            it[onboardingKey] ?: false
        }

    val isDarkMode: Flow<Boolean> =
        context.sessionDataStore.data.map {
            it[darkModeKey] ?: false
        }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.sessionDataStore.edit {
            it[darkModeKey] = enabled
        }
    }

    val notifications: Flow<List<AppNotification>> =
        context.sessionDataStore.data.map { preferences ->
            runCatching {
                gson.fromJson<List<AppNotification>>(
                    preferences[notificationsKey].orEmpty(),
                    object : TypeToken<List<AppNotification>>() {}.type,
                ) ?: emptyList()
            }.getOrDefault(emptyList())
        }

    suspend fun addNotification(notification: AppNotification) {
        context.sessionDataStore.edit { preferences ->
            val current = readNotifications(preferences[notificationsKey])
            preferences[notificationsKey] = gson.toJson(
                listOf(notification) + current.take(49),
            )
        }
    }

    suspend fun addNotificationIfAbsent(notification: AppNotification) {
        context.sessionDataStore.edit { preferences ->
            val current = readNotifications(preferences[notificationsKey])
            if (current.none { it.id == notification.id }) {
                preferences[notificationsKey] = gson.toJson(
                    listOf(notification) + current.take(49),
                )
            }
        }
    }

    suspend fun markNotificationsAsRead() {
        context.sessionDataStore.edit { preferences ->
            val current = readNotifications(preferences[notificationsKey])
            preferences[notificationsKey] = gson.toJson(current.map { it.copy(read = true) })
        }
    }

    private fun readNotifications(json: String?): List<AppNotification> =
        runCatching {
            gson.fromJson<List<AppNotification>>(
                json.orEmpty(),
                object : TypeToken<List<AppNotification>>() {}.type,
            ) ?: emptyList()
        }.getOrDefault(emptyList())

    suspend fun clear() {
        context.sessionDataStore.edit {
            it.remove(userIdKey)
            it[onboardingKey] = false
            it.remove(notificationsKey)
        }
    }
}