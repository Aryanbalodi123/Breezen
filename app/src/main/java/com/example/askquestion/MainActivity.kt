package com.example.askquestion

import android.os.Bundle
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
import com.example.askquestion.core.data.OnboardingPreferences
import com.example.askquestion.core.ui.navigation.AppNavHost
import com.example.askquestion.core.ui.theme.ASKQUESTIONTheme

class MainActivity : ComponentActivity() {
    private lateinit var onboardingPreferences: OnboardingPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.navigationBarColor = android.graphics.Color.BLACK

        onboardingPreferences = OnboardingPreferences(this)

        setContent {
            ASKQUESTIONTheme {
                // --- FIX: Define scope and context HERE ---
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                // --- END FIX ---

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    AppNavHost(onboardingPreferences)

//                    Button(
//                        onClick = {
//                            scope.launch {
//                                ColorUpdateService.runColorUpdate(context)
//                            }
//                        },
//                        modifier = Modifier
//                            .align(Alignment.BottomEnd)
//                            .padding(16.dp)
//                    ) {
//                        Text("RUN COLOR UPDATE")
//                    }
                    //
                }
            }
        }
    }
}