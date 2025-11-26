package com.example.breezen

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat // Make sure this is imported
import com.example.breezen.core.data.OnboardingPreferences
import com.example.breezen.core.network.Keys
import com.example.breezen.core.ui.navigation.AppNavHost
import com.example.breezen.core.ui.theme.BreezenTheme

class MainActivity : ComponentActivity() {
    private lateinit var onboardingPreferences: OnboardingPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Enable Edge to Edge BEFORE super.onCreate
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 2. FORCE content to extend into system bar areas (Transparent background)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 3. Set System Bars to Transparent
        window.navigationBarColor = Color.TRANSPARENT
        window.statusBarColor = Color.TRANSPARENT

        // 4. Disable contrast enforcement (removes the dark scrim/shadow on Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        Log.d("KEY_TEST", "Gemini key: " + Keys.getGeminiKey())
        Log.d("KEY_TEST", "Telegram key: " + Keys.getTelegramBotToken())
        Log.d("KEY_TEST", "Supabase key: " + Keys.getSupabaseAnonKey())

        onboardingPreferences = OnboardingPreferences(this)

        setContent {
            BreezenTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // Only pad the status bar.
                        // We DO NOT pad the bottom here, so the content goes behind the nav bar.
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    AppNavHost(onboardingPreferences)
                }
            }
        }
    }
}