package com.example.breezen.feature.meditation.model

import androidx.compose.ui.graphics.Color

data class GuidedMeditation(
    val title: String,
    val subtitle: String,
    val backgroundColor: Color,
    val titleColor: Color,
    val secondaryColor: Color,
    val vectorResId: Int,
    var currentIndex: Int
)

// Helper function for colors (Utils)
fun pastelShade(color: Color, factor: Float = 0.65f): Color {
    return Color(
        red = (color.red * factor),
        green = (color.green * factor),
        blue = (color.blue * factor),
        alpha = 1f
    )
}