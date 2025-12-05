package com.example.breezen

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.example.breezen.core.data.OnboardingPreferences
import com.example.breezen.core.ui.components.NoInternet
import com.example.breezen.core.ui.navigation.AppNavHost
import com.example.breezen.core.ui.theme.BreezenTheme
import com.example.breezen.core.ui.util.isInternetAvailable
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var onboardingPreferences: OnboardingPreferences

    @RequiresApi(Build.VERSION_CODES.O)
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

//        Log.d("KEY_TEST", "Gemini key: " + Keys.getGeminiKey())
//        Log.d("KEY_TEST", "Telegram key: " + Keys.getTelegramBotToken())
//        Log.d("KEY_TEST", "Supabase key: " + Keys.getSupabaseAnonKey())

        onboardingPreferences = OnboardingPreferences(this)

        setContent {
            BreezenTheme {
           InternetGate {
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
}

@Composable
fun InternetGate (mainScreen :@Composable () -> Unit) {

    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(isInternetAvailable(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            isOnline = isInternetAvailable(context)
            delay(3000)

        }


    }

    if (isOnline){
        mainScreen()
    }
    else{
        NoInternet()
    }



}