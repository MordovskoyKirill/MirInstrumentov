package com.example.mirinstrumentov.ui.auth

data class AuthUiState(
    val isCheckingSession: Boolean = true,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val email: String = "",
    val message: String? = null
)
