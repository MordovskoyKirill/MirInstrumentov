package com.example.mirinstrumentov.data.repository

import com.example.mirinstrumentov.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {
    private val auth get() = SupabaseClient.client.auth

    val sessionStatus: Flow<SessionStatus> get() = auth.sessionStatus

    fun currentEmail(): String? = auth.currentUserOrNull()?.email

    suspend fun signIn(emailAddress: String, userPassword: String) {
        auth.signInWith(Email) {
            email = emailAddress
            password = userPassword
        }
    }

    suspend fun signUp(fullName: String, emailAddress: String, userPassword: String): Boolean {
        val createdUser = auth.signUpWith(Email) {
            email = emailAddress
            password = userPassword
            data = buildJsonObject {
                put("full_name", fullName)
            }
        }
        return createdUser != null
    }

    suspend fun signOut() {
        auth.signOut()
    }
}
