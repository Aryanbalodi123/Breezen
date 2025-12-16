package com.example.breezen.feature.home

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.breezen.core.data.UserPreferences
import com.example.breezen.core.network.AuthService
import com.example.breezen.core.network.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    init {
        observeLocalUser()
        syncRemoteUser()
    }

    /**
     * 1. Continuously listen to DataStore.
     * Whenever a logout or signup happens elsewhere in the app,
     * this block will trigger automatically and update the UI.
     */
    private fun observeLocalUser() {
        viewModelScope.launch {
            // Using collectLatest instead of firstOrNull ensures we keep listening
            UserPreferences.getUsername(getApplication()).collectLatest { localName ->
                if (!localName.isNullOrEmpty()) {
                    _user.value = User(localName)
                } else {
                    // Optional: Handle the case where user is null (logged out state)
                    _user.value = null
                }
            }
        }
    }

    /**
     * 2. Sync with Supabase/Backend once on load.
     * If the remote data is different, we update DataStore.
     * The update to DataStore will trigger the observer in step 1.
     */
    private fun syncRemoteUser() {
        viewModelScope.launch {
            try {
                val remoteUser = AuthService.getCurrentUser()
                val currentLocalName = _user.value?.username

                if (remoteUser != null && remoteUser.username != currentLocalName) {
                    // We simply save to preferences.
                    // The observeLocalUser() function above will detect this change and update the UI.
                    UserPreferences.saveUsername(getApplication(), remoteUser.username)
                }
            } catch (_: Exception) {
                // Silent fallback
            }
        }
    }
}