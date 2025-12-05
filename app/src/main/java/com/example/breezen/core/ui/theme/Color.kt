package com.example.breezen.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// -----------------------------------------------------
//  BASE PALETTE
// -----------------------------------------------------
val AppBlack = Color(0xFF0A0A0A)
val AppWhite = Color(0xFFFFFFFF)
val AppGray = Color(0xFFB0B0B0) // Secondary text gray

val SolidBlack = Color.Black
// -----------------------------------------------------
//  BRAND COLORS
// -----------------------------------------------------
val BrandGreen = Color(0xFF78C841)
val BrandGreenBright = Color(0xFF00C853)
val BrandGreenDarker = Color(0xFF00695C)

// -----------------------------------------------------
//  TEXT COLORS
// -----------------------------------------------------
val TextPrimary = AppWhite
val TextSecondary = AppGray
val TextPlaceholder = Color(0xFF808080)

// -----------------------------------------------------
//  SYSTEM COLORS
// -----------------------------------------------------
val SystemError = Color(0xFFF48FB1)
val SystemWarning = Color(0xFFFFD54F)
val SystemStop = Color(0xFFFF6B6B)
val SystemPause = Color(0xFFFF9800)

// -----------------------------------------------------
//  WHITE ALPHA SHADES (GLASS, SURFACES, CARDS)
// -----------------------------------------------------
val WhiteAlpha03 = AppWhite.copy(alpha = 0.03f)
val WhiteAlpha05 = AppWhite.copy(alpha = 0.05f)
val WhiteAlpha06 = AppWhite.copy(alpha = 0.06f)
val WhiteAlpha08 = AppWhite.copy(alpha = 0.08f)
val WhiteAlpha10 = AppWhite.copy(alpha = 0.10f)
val WhiteAlpha12 = AppWhite.copy(alpha = 0.12f)
val WhiteAlpha15 = AppWhite.copy(alpha = 0.15f)
val WhiteAlpha20 = AppWhite.copy(alpha = 0.20f)

// -----------------------------------------------------
//  BLACK ALPHA SHADES (BACKDROP DIM, DARK GLASS)
// -----------------------------------------------------
val BlackAlpha20 = AppBlack.copy(alpha = 0.20f)
val BlackAlpha40 = AppBlack.copy(alpha = 0.40f)
val BlackAlpha60 = AppBlack.copy(alpha = 0.60f)
val BlackAlpha80 = AppBlack.copy(alpha = 0.80f)
val BlackAlpha90 = AppBlack.copy(alpha = 0.90f)

// -----------------------------------------------------
//  GUIDED MEDITATION PASTEL COLORS
// -----------------------------------------------------
val pastelColors = listOf(
    Color(0xFFFFC1CC),
    Color(0xFFFFD8B1),
    Color(0xFFC1FFD7),
    Color(0xFFC1F2FF),
    Color(0xFFE3C1FF),
    Color(0xFFFFF5C1),
    Color(0xFFC1E0FF),
    Color(0xFFFFCFC1),
)
//temp

val GlassBackground = WhiteAlpha10
val GlassBorder = WhiteAlpha20


// ---- Corner Radius System ----
// Consistent rounding across the entire app

val CornerXLarge = 32.dp     // Large glass cards, dialogs, surfaces
val CornerLarge = 20.dp      // Technique items, medium cards
val CornerMedium = 16.dp     // Input fields, small cards
val CornerSmall = 12.dp      // Minor rounded boxes
val CornerCircle = 50.dp     // Capsule shapes / pill buttons



// ---------------------------
// CHATBOT THEME COLORS
// ---------------------------


val LightGreen = Color(0xFFF1F8E9)
val DarkGreen = Color(0xFF0F291E)
val AccentGreen = Color(0xFF9CCC65)
val YellowAccent = Color(0xFFFFF59D)
val PureWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF546E7A)



// FEATURED SECTION
val DeepTeal = Color(0xFF012F46)          // Card 1 base
val MidnightBlue = Color(0xFF00090E)      // Dark blend layer
val OceanGreen = Color(0xFF07A796)        // Accent gradient on play button

val SunriseYellow = Color(0xFFDDE46F)     // Card 2 start tone
val MistGreen = Color(0xFF68A095)         // Mid green transition
val IndigoNight = Color(0xFF21366D)       // Deep blue blend
val CosmosBlackBlue = Color(0xFF111333)   // Final dark edge