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

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    init {
        loadUser()
    }

    /**
     * Loads username from DataStore, then optionally syncs with Supabase.
     * Keeps UI responsive even if network check fails.
     */
    private fun loadUser() {
        viewModelScope.launch {

            // Local username (fast load)
            val localName = UserPreferences.getUsername(getApplication()).firstOrNull()
            if (!localName.isNullOrEmpty()) {
                _user.value = User(localName)
            }

            // Remote profile sync (non-blocking, optional)
            try {
                val remoteUser = AuthService.getCurrentUser()

                if (remoteUser != null && remoteUser.username != localName) {
                    _user.value = remoteUser
                    UserPreferences.saveUsername(getApplication(), remoteUser.username)
                }

            } catch (_: Exception) {
                // Silent fallback — UI still works with local data
            }
        }
    }
}
