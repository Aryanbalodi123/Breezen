package com.example.breezen.feature.onboarding.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import kotlin.random.Random

// Data class to hold info for the drifting orbs
private data class Orb(
    val radius: Float,
    val color: Color,
    val initialX: Float,
    val initialY: Float,
    val animatableX: Animatable<Float, *>,
    val animatableY: Animatable<Float, *>
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BreathingOrbBackground() {

    // Get theme colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // 1. --- The Base Gradient ---
    val baseGradient = Brush.verticalGradient(
        colors = listOf(
            surfaceColor.copy(alpha = 0.5f),
            backgroundColor,
            backgroundColor
        ),
        startY = 0f,
        endY = 2000f
    )

    // 2. --- The Central "Breathing" Pulse ---
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse-alpha"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse-scale"
    )

    // Colors for the soft nebulae/orbs
    val orbColors = listOf(
        secondaryColor.copy(alpha = 0.05f),
        Color(0xFF4d4d00).copy(alpha = 0.07f), // Subtle, deep yellow
        primaryColor.copy(alpha = 0.05f),
        Color(0xFF004D40).copy(alpha = 0.07f)  // A deep teal
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp().toPx() }
        val screenHeight = with(LocalDensity.current) { constraints.maxHeight.toDp().toPx() }

        // 3. --- The Drifting Orbs ---
        val orbs = remember {
            List(5) { // Create 5 orbs
                val radius = Random.nextFloat() * 400f + 300f // Larger orbs
                Orb(
                    radius = radius,
                    color = orbColors.random(),
                    initialX = Random.nextFloat() * screenWidth,
                    initialY = Random.nextFloat() * screenHeight,
                    animatableX = Animatable(Random.nextFloat() * screenWidth),
                    animatableY = Animatable(Random.nextFloat() * screenHeight)
                )
            }
        }

        // Launch animation for each orb
        LaunchedEffect(orbs) {
            orbs.forEach { orb ->
                launch {
                    orb.animatableX.animateTo(
                        targetValue = (orb.initialX + Random.nextFloat() * 600f - 300f) % screenWidth,
                        animationSpec = infiniteRepeatable(
                            animation = tween(Random.nextInt(12000, 18000), easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }
                launch {
                    orb.animatableY.animateTo(
                        targetValue = (orb.initialY + Random.nextFloat() * 600f - 300f) % screenHeight,
                        animationSpec = infiniteRepeatable(
                            animation = tween(Random.nextInt(12000, 18000), easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }
            }
        }

        // 4. --- The Canvas ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Layer 1: Draw the base gradient
            drawRect(brush = baseGradient)

            // Layer 2: Draw the central "breathing" pulse
            drawSoftOrb(
                center = center,
                radius = size.width * 1.3f * pulseScale, // Scales with screen size
                color = Color(0xFF004D40).copy(alpha = 0.5f * pulseAlpha) // Deep, calm teal
            )

            // Layer 3: Draw the small drifting orbs
            orbs.forEach { orb ->
                drawSoftOrb(
                    center = Offset(orb.animatableX.value, orb.animatableY.value),
                    radius = orb.radius,
                    color = orb.color
                )
            }
        }
    }
}

// Helper function to draw a blurry, soft-edged orb
private fun DrawScope.drawSoftOrb(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}