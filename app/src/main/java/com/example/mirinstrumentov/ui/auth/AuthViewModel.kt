package com.example.mirinstrumentov.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirinstrumentov.data.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        // Supabase автоматически загружает сохранённую сессию.
        viewModelScope.launch {
            try {
                repository.sessionStatus.collect { session ->
                    _state.update { old ->
                        when (session) {
                            SessionStatus.Initializing -> old.copy(isCheckingSession = true)
                            is SessionStatus.Authenticated -> old.copy(
                                isCheckingSession = false,
                                isAuthenticated = true,
                                email = repository.currentEmail().orEmpty(),
                                message = null
                            )
                            is SessionStatus.NotAuthenticated -> old.copy(
                                isCheckingSession = false,
                                isAuthenticated = false,
                                email = ""
                            )
                            is SessionStatus.RefreshFailure -> old.copy(
                                isCheckingSession = false,
                                isAuthenticated = false,
                                email = "",
                                message = "Не удалось восстановить сессию. Войди снова."
                            )
                        }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update {
                    it.copy(isCheckingSession = false, message = error.message ?: "Ошибка соединения")
                }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    fun signIn(email: String, password: String) {
        if (_state.value.isLoading) return
        val cleanEmail = email.trim()
        val error = validateEmail(cleanEmail) ?: if (password.isBlank()) "Введи пароль" else null
        if (error != null) {
            _state.update { it.copy(message = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            try {
                repository.signIn(cleanEmail, password)
                // Экран переключится автоматически благодаря sessionStatus.
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(message = "Ошибка входа: ${error.message ?: "проверь данные"}") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signUp(fullName: String, email: String, password: String, confirm: String) {
        if (_state.value.isLoading) return
        val cleanEmail = email.trim()
        val cleanName = fullName.trim()
        val error = when {
            cleanName.isEmpty() -> "Введи ФИО"
            validateEmail(cleanEmail) != null -> validateEmail(cleanEmail)
            password.length < 6 -> "Пароль должен быть не короче 6 символов"
            password != confirm -> "Пароли не совпадают"
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(message = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            try {
                val mustVerify = repository.signUp(cleanName, cleanEmail, password)
                if (mustVerify) {
                    _state.update {
                        it.copy(message = "Аккаунт создан. Подтверди email через письмо, затем войди.")
                    }
                }
                // Если подтверждение отключено, sessionStatus откроет главную страницу.
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(message = "Ошибка регистрации: ${error.message ?: "попробуй ещё раз"}") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signOut() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            try {
                repository.signOut()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(message = "Ошибка выхода: ${error.message ?: "попробуй ещё раз"}") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Введи email"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Некорректный email"
        else -> null
    }
}
