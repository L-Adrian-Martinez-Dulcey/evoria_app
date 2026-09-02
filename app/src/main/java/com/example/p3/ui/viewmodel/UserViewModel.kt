package com.example.p3.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.p3.data.api.RetrofitClient
import com.example.p3.data.model.User
import com.example.p3.data.repository.UserRepository
import com.example.p3.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(RetrofitClient.apiService)
    private val sessionManager = SessionManager(application)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loginResult = MutableStateFlow<User?>(null)
    val loginResult: StateFlow<User?> = _loginResult

    // Representa la sesión mientras la aplicación permanece abierta.
    val currentUser: StateFlow<User?> = _loginResult

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError


    init {
        fetchUsers()
        restoreSession()
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _users.value = repository.getUsers()
                _error.value = null
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addUser(name: String, email: String) {
        viewModelScope.launch {
            try {
                repository.createUser(User(name = name, email = email))
                fetchUsers()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateUser(id: String, name: String, email: String) {
        viewModelScope.launch {
            try {
                repository.updateUser(id, User(id = id, name = name, email = email))
                fetchUsers()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun restoreSession() = viewModelScope.launch {
        try {
            val userId = sessionManager.getUserId() ?: return@launch
            _loginResult.value = repository.getUser(userId)
        } catch (_: Exception) {
            // Si el usuario fue eliminado de MockAPI, se evita una sesiÃ³n invÃ¡lida.
            sessionManager.clear()
        }
    }

    fun updateProfile(user: User, onSuccess: () -> Unit) {
        if (user.name.isBlank() || user.email.isBlank() || user.phone.isBlank() || user.city.isBlank()) {
            _error.value = "Completa todos los campos del perfil"
            return
        }
        viewModelScope.launch {
            try {
                val updated = repository.updateUser(requireNotNull(user.id), user)
                _loginResult.value = updated
                sessionManager.saveUserId(requireNotNull(updated.id))
                _users.value = _users.value.map { if (it.id == updated.id) updated else it }
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "No fue posible actualizar el perfil: ${e.message}"
            }
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteUser(id)
                fetchUsers()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val users = repository.getUserByEmail(email)

                if (users.isEmpty()) {
                    _loginError.value = "Usuario no encontrado"
                    _loginResult.value = null
                    return@launch
                }

                val user = users.first()

                if (user.password == password) {
                    _loginResult.value = user
                    user.id?.let { sessionManager.saveUserId(it) }
                    _loginError.value = null
                } else {
                    _loginError.value = "Contraseña incorrecta"
                    _loginResult.value = null
                }

            } catch (e: Exception) {
                _loginError.value = "Error de conexión"
                _loginResult.value = null
            }
        }
    }

    fun logout(onComplete: () -> Unit) = viewModelScope.launch {
        sessionManager.clear()
        _loginResult.value = null
        _loginError.value = null
        onComplete()
    }

}
