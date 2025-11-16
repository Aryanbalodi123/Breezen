package com.example.askquestion.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// We create our app's dark color scheme using the palette from Color.kt
private val AppDarkColorScheme = darkColorScheme(
    primary = BrandGreen,           // Your main action color
    secondary = BrandGreenBright,   // A secondary accent

    background = AppBlack,
    surface = GlassBackground,      // This will be your "glassy" card color

    error = SystemError,            // Your pink error color

    onPrimary = AppBlack,           // Text on top of BrandGreen
    onSecondary = AppBlack,         // Text on top of BrandGreenBright
    onBackground = TextPrimary,     // Main text color (White)
    onSurface = TextSecondary,      // Body text color on cards (Gray)
    onError = AppBlack
)

@Composable
fun ASKQUESTIONTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AppDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Use our official typography
        content = content
    )
}