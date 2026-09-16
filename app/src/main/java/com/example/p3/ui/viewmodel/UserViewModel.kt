package com.example.p3.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.p3.data.api.GithubRetrofitClient
import com.example.p3.data.api.RetrofitClient
import com.example.p3.data.model.User
import com.example.p3.data.repository.ImageRepository
import com.example.p3.data.repository.UserRepository
import com.example.p3.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(RetrofitClient.apiService)
    private val imageRepository = ImageRepository(
        application.contentResolver,
        GithubRetrofitClient.apiService,
    )
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

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted = _isOnboardingCompleted.asStateFlow()

    private val _sessionChecked = MutableStateFlow(false)
    val sessionChecked = _sessionChecked.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    init {
        fetchUsers()
        restoreSessionAndOnboarding()
    }

    private fun restoreSessionAndOnboarding() = viewModelScope.launch {
        // Restaurar sesión
        try {
            val userId = sessionManager.getUserId()
            if (userId != null) {
                _loginResult.value = repository.getUser(userId)
            }
        } catch (_: Exception) {
            sessionManager.clear()
        }

        // Observar onboarding (esto emitirá el valor actual inmediatamente)
        sessionManager.isOnboardingCompleted.collectLatest {
            _isOnboardingCompleted.value = it
            _sessionChecked.value = true
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
                repository.createUser(User(name = name, email = email))
                fetchUsers()
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
        val normalizedEmail = email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            _error.value = "Ingresa un correo válido"
            return
        }
        if (password.length < 8) {
            _error.value = "La contraseña debe tener al menos 8 caracteres"
            return
        }
        if (name.trim().length !in 2..80 || phone.trim().length !in 7..20 || city.trim().length !in 2..80) {
            _error.value = "Verifica la longitud de tus datos"
            return
        }
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (repository.getUserByEmail(normalizedEmail).isNotEmpty()) {
                    _error.value = "Ya existe una cuenta con ese correo"
                    return@launch
                }
                val user = User(
                    name = name.trim(),
                    email = normalizedEmail,
                    password = password,
                    phone = phone,
                    city = city
                )
                repository.createUser(user)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "No fue posible crear la cuenta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(id: String, name: String, email: String) {
        viewModelScope.launch {
            try {
                val current = repository.getUser(id)
                repository.updateUser(id, current.copy(name = name.trim(), email = email.trim().lowercase()))
                fetchUsers()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateProfile(user: User, selectedImageUri: Uri? = null, onSuccess: () -> Unit) {
        if (user.name.trim().length !in 2..80 ||
            !Patterns.EMAIL_ADDRESS.matcher(user.email.trim()).matches() ||
            user.phone.trim().length !in 7..20 ||
            user.city.trim().length !in 2..80
        ) {
            _error.value = "Completa correctamente los datos del perfil"
            return
        }
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = requireNotNull(user.id)
                val normalizedEmail = user.email.trim().lowercase()
                if (repository.getUserByEmail(normalizedEmail).any { it.id != userId }) {
                    _error.value = "Ya existe una cuenta con ese correo"
                    return@launch
                }
                val imageUrl = selectedImageUri?.let {
                    imageRepository.uploadUserImage(it, userId)
                }
                val updated = repository.updateUser(
                    userId,
                    user.copy(
                        name = user.name.trim(),
                        email = normalizedEmail,
                        phone = user.phone.trim(),
                        city = user.city.trim(),
                        avatar = imageUrl ?: user.avatar,
                    ),
                )
                _loginResult.value = updated
                sessionManager.saveUserId(requireNotNull(updated.id))
                _users.value = _users.value.map { if (it.id == updated.id) updated else it }
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "No fue posible actualizar el perfil: ${e.message}"
            } finally {
                _isLoading.value = false
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
        val normalizedEmail = email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() || password.isBlank()) {
            _loginError.value = "Ingresa un correo y contraseña válidos"
            return
        }
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val users = repository.getUserByEmail(normalizedEmail)

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
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(onComplete: () -> Unit) = viewModelScope.launch {
        sessionManager.clear()
        _loginResult.value = null
        _loginError.value = null
        _isOnboardingCompleted.value = false // Reflejar el cambio en memoria inmediatamente
        onComplete()
    }

}
