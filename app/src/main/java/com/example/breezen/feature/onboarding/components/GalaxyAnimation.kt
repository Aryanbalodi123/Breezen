package com.example.breezen.feature.onboarding.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- Enums and Data Classes ---

enum class ShapeType { FILLED_CIRCLE, OUTLINED_CIRCLE, RHOMBUS }

data class OrbitingShape(
    val color: Color,
    val radius: Float,
    val size: Float,
    val speedMultiplier: Float,
    val shapeType: ShapeType,
    val orbitLineColor: Color,
    val direction: Int,
    val angleOffset: Float = 0f
)

// --- Galaxy Animation Code ---

internal fun createOrbitShapes(): List<OrbitingShape> {
    // This animation is highly artistic. Using hardcoded grays is intentional
    // and part of the design. We will not change these to theme colors.
    val colors = listOf(
        0xFF1A1A1A, 0xFF282828, 0xFF363636, 0xFF464646,
        0xFF565656, 0xFF686868, 0xFF7A7A7A, 0xFF8C8C8C,
        0xFF9E9E9E, 0xFFB0B0B0, 0xFFC4C4C4, 0xFFFFFFFF
    )
    val orbitColors = listOf(
        0xFF0D0D0D, 0xFF1A1A1A, 0xFF282828, 0xFF363636,
        0xFF464646, 0xFF565656, 0xFF686868, 0xFF7A7A7A,
        0xFF8C8C8C, 0xFF9E9E9E, 0xFFB0B0B0, 0xFFC4C4C4
    )
    val radii = listOf(150f, 210f, 279f, 358f, 449f, 554f, 675f, 814f, 974f, 1158f, 1370f, 1614f)
    val speeds =
        listOf(1.0f, 0.91f, 0.82f, 0.73f, 0.64f, 0.55f, 0.46f, 0.37f, 0.28f, 0.19f, 0.12f, 0.06f)
    val shapesPerOrbit = listOf(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

    return buildList {
        for (orbitIndex in radii.indices) {
            val numShapes = shapesPerOrbit[orbitIndex]
            val angleIncrement = 360f / numShapes
            val orbitDirection = if (Random.nextBoolean()) 1 else -1

            for (shapeIndex in 0 until numShapes) {
                val angle = angleIncrement * shapeIndex
                val shapeType = ShapeType.values().random()
                val sizeVariance = Random.nextFloat() * 0.4f + 0.8f
                val size = when (shapeType) {
                    ShapeType.FILLED_CIRCLE -> (14f + orbitIndex * 0.4f) * sizeVariance
                    ShapeType.OUTLINED_CIRCLE -> (20f + orbitIndex * 0.8f) * sizeVariance
                    ShapeType.RHOMBUS -> (18f + orbitIndex * 0.6f) * sizeVariance
                }
                val speedVariance = Random.nextFloat() * 0.3f + 0.85f
                val randomSpeedMultiplier = speeds[orbitIndex] * speedVariance

                add(
                    OrbitingShape(
                        color = Color(colors[orbitIndex]),
                        radius = radii[orbitIndex],
                        size = size,
                        speedMultiplier = randomSpeedMultiplier,
                        shapeType = shapeType,
                        orbitLineColor = Color(orbitColors[orbitIndex]),
                        direction = orbitDirection,
                        angleOffset = angle
                    )
                )
            }
        }
    }
}

@Composable
fun GalaxyAnimation(modifier: Modifier = Modifier, shapes: List<OrbitingShape>) {
    val infiniteTransition = rememberInfiniteTransition(label = "galaxy_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(90000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    // Get the background color from the theme
    val backgroundColor = MaterialTheme.colorScheme.background

    Canvas(modifier = modifier) {
        val centerX = -size.width * 0.2f
        val centerY = -size.height * 0.2f
        val uniqueRadii = shapes.map { it.radius }.distinct().sorted()
        uniqueRadii.forEachIndexed { index, radius ->
            val orbitColor =
                shapes.firstOrNull { it.radius == radius }?.orbitLineColor ?: Color.Gray
            drawOrbitLine(orbitColor.copy(alpha = 0.5f), radius, centerX, centerY)
        }
        shapes.forEach { shape ->
            drawOrbitingShape(shape, angle, centerX, centerY, backgroundColor)
        }
    }
}

internal fun DrawScope.drawOrbitLine(color: Color, radius: Float, centerX: Float, centerY: Float) {
    drawOval(
        color = color,
        topLeft = Offset(centerX - radius, centerY - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 1.dp.toPx())
    )
}

internal fun DrawScope.drawOrbitingShape(
    shape: OrbitingShape,
    angle: Float,
    centerX: Float,
    centerY: Float,
    backgroundColor: Color
) {
    val currentAngle = Math.toRadians(
        ((angle * shape.speedMultiplier * shape.direction) + shape.angleOffset).toDouble()
    )
    val x = centerX + shape.radius * cos(currentAngle).toFloat()
    val y = centerY + shape.radius * sin(currentAngle).toFloat()

    when (shape.shapeType) {
        ShapeType.FILLED_CIRCLE -> {
            drawCircle(color = shape.color, radius = shape.size, center = Offset(x, y))
        }

        ShapeType.OUTLINED_CIRCLE -> {
            drawCircle(color = backgroundColor, radius = shape.size, center = Offset(x, y))
            drawCircle(
                color = shape.color,
                radius = shape.size,
                center = Offset(x, y),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        ShapeType.RHOMBUS -> {
            val widthHalf = shape.size * 0.7f
            val heightHalf = shape.size * 1.4f
            val path = Path().apply {
                moveTo(x, y - heightHalf)
                lineTo(x + widthHalf, y)
                lineTo(x, y + heightHalf)
                lineTo(x - widthHalf, y)
                close()
            }
            drawPath(path, color = backgroundColor)
            drawPath(path, color = shape.color, style = Stroke(width = 2.5.dp.toPx()))
        }
    }
}