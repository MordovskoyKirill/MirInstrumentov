package com.example.mirinstrumentov.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Цвета из макета
private val Blue = Color(0xFF1976EB)
private val Background = Color(0xFFF5F5F5)
private val Gray = Color(0xFF69748B)
private val FieldBorder = Color(0xFF6A768E)
private val White = Color.White

@Composable
fun AuthScreen(
    state: AuthUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onClearMessage: () -> Unit,
    initialRegisterMode: Boolean = false
) {

    var registerMode by rememberSaveable {
        mutableStateOf(initialRegisterMode)
    }

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var fullName by rememberSaveable {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Column(
                modifier = Modifier
                    .widthIn(max = 350.dp)
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(
                        color = White,
                        shape = RoundedCornerShape(21.dp)
                    )
                    .padding(horizontal = 20.dp)
            ) {

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {

                    AuthTab(
                        title = "Вход",
                        selected = !registerMode,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            registerMode = false
                            password = ""
                            confirmPassword = ""
                            onClearMessage()
                        }
                    )

                    AuthTab(
                        title = "Регистрация",
                        selected = registerMode,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            registerMode = true
                            password = ""
                            confirmPassword = ""
                            onClearMessage()
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(
                        if (registerMode) 39.dp else 96.dp
                    )
                )

                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    hint = "Email",
                    keyboardType = KeyboardType.Email
                    ,
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(15.dp))

                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    hint = "Пароль",
                    isPassword = true,
                    enabled = !state.isLoading
                )

                if (registerMode) {

                    Spacer(modifier = Modifier.height(15.dp))

                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                        },
                        hint = "Подтвердите пароль",
                        isPassword = true,
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    AuthTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                        },
                        hint = "ФИО",
                        enabled = !state.isLoading
                    )
                }

                // Ошибки Supabase и проверки данных
                if (state.message != null) {

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.message,
                        color = Color(0xFFD32F2F),
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                        ,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Кнопка входа / регистрации
                Button(
                    onClick = {

                        if (registerMode) {

                            onRegister(
                                fullName,
                                email,
                                password,
                                confirmPassword
                            )

                        } else {

                            onLogin(
                                email,
                                password
                            )
                        }
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        contentColor = White,
                        disabledContainerColor = Blue.copy(
                            alpha = 0.6f
                        )
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {

                    if (state.isLoading) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )

                    } else {

                        Text(
                            text = if (registerMode) {
                                "Создать аккаунт"
                            } else {
                                "Войти"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun AuthTab(
    title: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(enabled = enabled) {
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Blue else Gray,
            maxLines = 1
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (selected) 2.dp else 1.dp)
                .background(
                    if (selected) Blue else Gray
                )
        )
    }
}

// Поле ввода по дизайну Figma
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                color = White,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = FieldBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.CenterStart
    ) {

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                color = Color(0xFF222222),
                fontSize = 19.sp
            ),
            cursorBrush = SolidColor(Blue),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) {
                    KeyboardType.Password
                } else {
                    keyboardType
                }
            ),
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            decorationBox = { innerTextField ->

                Box(
                    contentAlignment = Alignment.CenterStart
                ) {

                    if (value.isEmpty()) {

                        Text(
                            text = hint,
                            color = Gray,
                            fontSize = 19.sp,
                            maxLines = 1
                        )
                    }

                    innerTextField()
                }
            }
        )
    }
}

@Preview(
    name = "Авторизация",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)
@Composable
private fun LoginPreview() {

    AuthScreen(
        state = AuthUiState(
            isCheckingSession = false
        ),
        onLogin = { _, _ -> },
        onRegister = { _, _, _, _ -> },
        onClearMessage = {},
        initialRegisterMode = false
    )
}

@Preview(
    name = "Регистрация",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)
@Composable
private fun RegistrationPreview() {

    AuthScreen(
        state = AuthUiState(
            isCheckingSession = false
        ),
        onLogin = { _, _ -> },
        onRegister = { _, _, _, _ -> },
        onClearMessage = {},
        initialRegisterMode = true
    )
}