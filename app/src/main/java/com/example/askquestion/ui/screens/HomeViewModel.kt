package com.example.askquestion.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askquestion.network.AuthService
import com.example.askquestion.network.User
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            try {
                _user.value = AuthService.getCurrentUser()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
