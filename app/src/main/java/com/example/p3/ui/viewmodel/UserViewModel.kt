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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(RetrofitClient.apiService)
    private val sessionManager = SessionManager(application)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loginResult = MutableStateFlow<User?>(null)
    val loginResult: StateFlow<User?> = _loginResult

    // Representa la sesión mientras la aplicación permanece abierta.
    val currentUser: StateFlow<User?> = _loginResult

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    val isOnboardingCompleted = _isOnboardingCompleted.asStateFlow()

    private val _sessionChecked = MutableStateFlow(false)

    val sessionChecked = _sessionChecked.asStateFlow()

    init {
        restoreSession()
        observeOnboarding()
    }

    private fun observeOnboarding() = viewModelScope.launch {
        sessionManager.isOnboardingCompleted.collectLatest {
            _isOnboardingCompleted.value = it
        }
    }

    fun completeOnboarding() = viewModelScope.launch {
        sessionManager.saveOnboardingCompleted()
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
                val created = repository.createUser(User(name = name, email = email))
                _users.value = _users.value + created
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun registerUser(
        name: String,
        email: String,
        password: String,
        phone: String,
        city: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = User(
                    name = name,
                    email = email,
                    password = password,
                    phone = phone,
                    city = city
                )

                repository.createUser(user)

                _error.value = null
                onSuccess()

            } catch (e: Exception) {
                _error.value = "No fue posible crear la cuenta: ${e.message}"
            }
        }
    }

    fun updateUser(id: String, name: String, email: String) {
        viewModelScope.launch {
            try {
                val updated = repository.updateUser(id, User(id = id, name = name, email = email))
                _users.value = _users.value.map { if (it.id == id) updated else it }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun restoreSession() = viewModelScope.launch {
        try {
            val userId = sessionManager.getUserId()

            if (userId != null) {
                _loginResult.value = repository.getUser(userId)
            }
        } catch (_: Exception) {
            sessionManager.clear()
            _loginResult.value = null
        } finally {
            _sessionChecked.value = true
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
                _users.value = _users.value.filterNot { it.id == id }
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

            } catch (_: Exception) {
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
