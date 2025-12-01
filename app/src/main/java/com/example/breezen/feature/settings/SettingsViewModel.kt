package com.example.breezen.feature.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breezen.core.network.AuthService
import com.example.breezen.core.network.FeedbackBody
import com.example.breezen.core.network.RetroFitClient
import com.example.breezen.core.network.SUPABASE_API_KEY_ANON
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    private fun setState(block: SettingsState.() -> SettingsState) {
        _state.value = _state.value.block()
    }

    /**
     * Update password via Supabase AuthService
     */
    suspend fun securePasswordUpdate(
        currentPassword: String,
        newPassword: String,
        context: android.content.Context
    ): String? {
        return try {
            setState { copy(isLoading = true, error = null) }

            // 1. Re-auth user first
            val email = AuthService.getCurrentUser()?.email
                ?: return "Reauthentication failed."

            AuthService.signIn(email, currentPassword) // <- real reauth

            // 2. Now update password
            AuthService.client.auth.updateUser {
                password = newPassword
            }

            setState { copy(isLoading = false) }
            null // success

        } catch (e: Exception) {
            setState { copy(isLoading = false) }
            e.message ?: "An error occurred"
        }
    }



    fun clearMessages() {
        setState { copy(message = null, error = null) }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                AuthService.logout()
            } catch (e: Exception) {
                Log.e("SettingsVM", "logout failed", e)
            } finally {
                onComplete()
            }
        }
    }

    suspend fun sendUserFeedback(feedback: String): Boolean {
        return try {
            RetroFitClient.api.sendFeedback(
                apiKey = SUPABASE_API_KEY_ANON,
                auth = "Bearer $SUPABASE_API_KEY_ANON",
                body = FeedbackBody(feedback)
            )
            true
        } catch (e: Exception) {
            false
        }
    }

}
