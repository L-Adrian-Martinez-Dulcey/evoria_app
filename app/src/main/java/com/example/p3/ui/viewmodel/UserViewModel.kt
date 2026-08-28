package com.example.p3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p3.data.api.RetrofitClient
import com.example.p3.data.model.User
import com.example.p3.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val repository = UserRepository(RetrofitClient.apiService)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loginResult = MutableStateFlow<User?>(null)
    val loginResult: StateFlow<User?> = _loginResult

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError


    init {
        fetchUsers()
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

}