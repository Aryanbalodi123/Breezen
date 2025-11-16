package com.example.askquestion.core.ui.theme

import androidx.compose.ui.graphics.Color

// ---- Base Palette ----
val AppBlack = Color(0xFF0A0A0A)
val AppWhite = Color(0xFFFFFFFF)
val AppGray = Color(0xFFB0B0B0) // For secondary text

// ---- Brand Green (Most common action color) ----
// Used in Auth, Chat, Send Button, etc.
val BrandGreen = Color(0xFF69F0AE)

// ---- Secondary Green (From "Breezen" header) ----
val BrandGreenBright = Color(0xFF00C853)

// ---- Darker Green (From Auth buttons) ----
val BrandGreenDarker = Color(0xFF00695C)

// ---- Text ----
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)
val TextPlaceholder = Color(0xFF808080)

// ---- System Colors ----
val SystemError = Color(0xFFF48FB1) // From your Auth screen
val SystemWarning = Color(0xFFFFD54F) // From password strength
val SystemStop = Color(0xFFFF6B6B) // From Breathe screen
val SystemPause = Color(0xFFFF9800) // From Breathe screen

// ---- Glass UI (From BottomNav) ----
val GlassBackground = Color.White.copy(alpha = 0.1f)
val GlassBorder = Color.White.copy(alpha = 0.2f)