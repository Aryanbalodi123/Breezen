package com.example.breezen.feature.home

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.breezen.core.data.UserPreferences
import com.example.breezen.core.network.AuthService
import com.example.breezen.core.network.User
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// Change to AndroidViewModel to get 'application' context
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            // STEP 1: Fast Load from DataStore
            // This runs instantly and fixes the "No session found" blank header
            val localName = UserPreferences.getUsername(getApplication()).firstOrNull()
            if (!localName.isNullOrEmpty()) {
                _user.value = User(localName)
            }

            // STEP 2: Background Check (Optional)
            // You can still check Supabase to ensure session is valid or update the name
            try {
                val remoteUser = AuthService.getCurrentUser()
                if (remoteUser != null && remoteUser.username != localName) {
                    // If remote name is different, update UI and DataStore
                    _user.value = remoteUser
                    UserPreferences.saveUsername(getApplication(), remoteUser.username)
                }
            } catch (e: Exception) {
                // If Supabase fails, we still have the localName displayed!
            }
        }
    }
}