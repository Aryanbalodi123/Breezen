package com.example.breezen.core.network

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
data class User(val username: String, val email: String? = null)

object AuthService {

    // Assuming these constants are defined in your project
    val client = createSupabaseClient(
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
            client.auth.sessionStatus.first { status ->
                status !is SessionStatus.Initializing
            }

            val user = client.auth.currentUserOrNull() ?: return null

            val username = user.userMetadata?.get("username")
                ?.jsonPrimitive
                ?.content ?: "Breeze User"

            val email = user.email

            User(username, email)

        } catch (e: Exception) {
            Log.e("AuthService", "Error getting current user", e)
            null
        }
    }

//    suspend fun updateProfile(username: String?, email: String?) {
//        try {
//            // Replaced modifyUser with updateUser
//            client.auth.updateUser {
//                if (!email.isNullOrBlank()) {
//                    this.email = email
//                }
//                // Update metadata for username
//                if (!username.isNullOrBlank()) {
//                    this.data = buildJsonObject {
//                        put("username", username)
//                    }
//                }
//            }
//            Log.d("AuthService", "Profile updated successfully")
//        } catch (e: Exception) {
//            Log.e("AuthService", "Failed to update profile", e)
//            throw e
//        }
//    }

    suspend fun logout() {
        try {
            client.auth.signOut()
        } catch (e: Exception) {
            Log.e("AuthService", "Logout failed", e)
        }
    }
}