package com.example.breezen.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class OnboardingPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit { putBoolean("onboarding_completed", completed) }
    }
}