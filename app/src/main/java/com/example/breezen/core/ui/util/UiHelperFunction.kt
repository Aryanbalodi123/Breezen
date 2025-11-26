package com.example.breezen.core.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun Modifier.gradientBackground(colorList: List<Color>, angle: Float) = this.then(
    Modifier.drawBehind {

        val angleRad = angle * (PI / 180f)

        // Direction vector
        val x = cos(angleRad).toFloat()
        val y = sin(angleRad).toFloat()

        // Half-diagonal of the box
        val radius = sqrt(size.width.pow(2) + size.height.pow(2)) / 2f

        // Start and end points of the gradient line
        val start = Offset(
            x = center.x - x * radius,
            y = center.y - y * radius
        )

        val end = Offset(
            x = center.x + x * radius,
            y = center.y + y * radius
        )

        // Draw the rectangle with linear gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = colorList,
                start = start,
                end = end
            ),
            size = size
        )
    }
)
