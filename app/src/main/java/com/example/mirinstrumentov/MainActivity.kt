package com.example.mirinstrumentov

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mirinstrumentov.ui.auth.AuthScreen
import com.example.mirinstrumentov.ui.auth.AuthViewModel
import com.example.mirinstrumentov.ui.home.HomeScreen
import com.example.mirinstrumentov.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val authViewModel: AuthViewModel = viewModel()
                val state by authViewModel.state.collectAsStateWithLifecycle()
                when {
                    state.isCheckingSession -> LoadingScreen()
                    state.isAuthenticated -> HomeScreen(
                        email = state.email,
                        isLoading = state.isLoading,
                        message = state.message,
                        onLogout = authViewModel::signOut
                    )
                    else -> AuthScreen(
                        state = state,
                        onLogin = authViewModel::signIn,
                        onRegister = authViewModel::signUp,
                        onClearMessage = authViewModel::clearMessage
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text("Проверяем авторизацию", modifier = Modifier.padding(16.dp))
    }
}
