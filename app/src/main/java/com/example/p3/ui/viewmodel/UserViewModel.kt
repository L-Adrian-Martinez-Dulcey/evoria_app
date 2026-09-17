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

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError

    private val _loginResult = MutableStateFlow<User?>(null)
    val loginResult: StateFlow<User?> = _loginResult

    // Representa la sesión mientras la aplicación permanece abierta.
    val currentUser: StateFlow<User?> = _loginResult

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted = _isOnboardingCompleted.asStateFlow()

    private val _sessionChecked = MutableStateFlow(false)
    val sessionChecked = _sessionChecked.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val enabled = !_isDarkMode.value
        _isDarkMode.value = enabled
        viewModelScope.launch {
            sessionManager.saveDarkMode(enabled)
        }
    }

    init {
        fetchUsers()
        restoreSessionAndOnboarding()
        viewModelScope.launch {
            sessionManager.isDarkMode.collectLatest { _isDarkMode.value = it }
        }
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
            _authError.value = "Ingresa un correo válido"
            return
        }
        if (password.length < 8) {
            _authError.value = "La contraseña debe tener al menos 8 caracteres"
            return
        }
        if (name.trim().length !in 2..80 || phone.trim().length !in 7..20 || city.trim().length !in 2..80) {
            _authError.value = "Verifica la longitud de tus datos"
            return
        }
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                if (name.isBlank() || phone.isBlank() || city.isBlank()) {
                    _authError.value = "Completa todos los campos"
                    return@launch
                }
                if (password.length < 8) {
                    _authError.value = "La contraseña debe tener al menos 8 caracteres"
                    return@launch
                }
                if (repository.getUserByEmail(normalizedEmail).isNotEmpty()) {
                    _authError.value = "Ya existe una cuenta con ese correo"
                    return@launch
                }
                val user = User(
                    name = name.trim(),
                    email = normalizedEmail,
                    password = password,
                    phone = phone.trim(),
                    city = city.trim()
                )
                repository.createUser(user)
                _authError.value = null
                onSuccess()
            } catch (e: Exception) {
                _authError.value = "No fue posible crear la cuenta: ${e.message}"
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
            _profileError.value = "Completa correctamente los datos del perfil"
            return
        }
        val normalizedEmail = user.email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            _profileError.value = "Ingresa un correo válido"
            return
        }
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _profileError.value = null
            try {
                val userId = requireNotNull(user.id)
                val normalizedEmail = user.email.trim().lowercase()
                val emailOwner = repository.getUserByEmail(normalizedEmail)
                    .firstOrNull { it.id != userId }
                if (emailOwner != null) {
                    _profileError.value = "Ya existe una cuenta con ese correo"
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
                _profileError.value = null
                onSuccess()
            } catch (e: Exception) {
                _profileError.value = "No fue posible actualizar el perfil: ${e.message}"
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
            _loginError.value = null
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

    fun clearErrors() {
        _error.value = null
        _authError.value = null
        _profileError.value = null
        _loginError.value = null
    }

    fun logout(onComplete: () -> Unit) = viewModelScope.launch {
        sessionManager.clear()
        _loginResult.value = null
        _loginError.value = null
        _isOnboardingCompleted.value = false // Reflejar el cambio en memoria inmediatamente
        onComplete()
    }

}
