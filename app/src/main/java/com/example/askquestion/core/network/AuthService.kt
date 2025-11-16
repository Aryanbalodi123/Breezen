package com.example.askquestion.core.network

import android.util.Log
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable
data class User(val username: String)

object AuthService {

    private val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL_AUTH,
        supabaseKey = SUPABASE_API_KEY_ANON
    ) {
        install(Auth)
    }

    suspend fun signUp(username: String, email: String, password: String) {
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("username", username)
                }
            }
            Log.d("AuthService", "Sign up successful for email: $email")
        } catch (e: Exception) {
            Log.e("AuthService", "Sign up failed for email: $email", e)
            throw e
        }
    }

    suspend fun signIn(email: String, password: String) {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Log.d("AuthService", "Sign in successful for email: $email")
        } catch (e: Exception) {
            Log.e("AuthService", "Sign in failed for email: $email", e)
            throw e
        }
    }

    suspend fun getCurrentUser(): User? {
        return try {
            // Wait until Supabase Auth is fully initialized
            client.auth.sessionStatus.first { status ->
                status !is SessionStatus.Initializing
            }

            val user = client.auth.currentUserOrNull() ?: return null

            val username = user.userMetadata?.get("username")
                ?.jsonPrimitive
                ?.content

            username?.let { User(it) }

        } catch (e: Exception) {
            Log.e("AuthService", "Error getting current user", e)
            null
        }
    }

}