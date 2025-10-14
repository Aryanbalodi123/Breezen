package com.example.galaxyanimation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// -------------------------------------------------------------
// 🎨 Shape Type Enum
// -------------------------------------------------------------
enum class ShapeType {
    FILLED_CIRCLE,
    OUTLINED_CIRCLE,
    RHOMBUS
}

// -------------------------------------------------------------
// 🌌 Orbiting Shape Data
// -------------------------------------------------------------
data class OrbitingShape(
    val color: Color,
    val radius: Float,
    val size: Float,
    val speedMultiplier: Float,
    val shapeType: ShapeType,
    val orbitLineColor: Color,
    val angleOffset: Float
)

// -------------------------------------------------------------
// 🌠 Create Orbits and Shapes
// -------------------------------------------------------------
fun createOrbitShapes(): List<OrbitingShape> {
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

    val radii = listOf(150f, 230f, 320f, 420f, 540f, 680f, 850f, 1050f, 1280f, 1550f, 1850f, 2200f)
    val shapesPerOrbit = listOf(3, 3, 4, 4, 5, 5, 6, 6, 7, 8, 8, 9)

    return buildList {
        for (orbitIndex in radii.indices) {
            val orbitRadius = radii[orbitIndex]
            val orbitColor = Color(orbitColors[orbitIndex])
            val orbitBaseSpeed = 0.05f + (0.01f * orbitIndex) // slow base rotation

            val totalShapes = shapesPerOrbit[orbitIndex]
            val angleStep = 360f / totalShapes
            val usedAngles = mutableListOf<Float>()

            repeat(totalShapes) { shapeIndex ->
                // Prevent overlap with random angle but spaced
                var angle: Float
                do {
                    angle = (0..359).random().toFloat()
                } while (usedAngles.any { abs((it - angle + 360) % 360) < angleStep * 0.6 })
                usedAngles.add(angle)

                val direction = if ((0..1).random() == 0) 1 else -1
                val speedMultiplier = orbitBaseSpeed * (0.8f + Math.random().toFloat() * 0.4f) * direction

                val shapeType = when (shapeIndex % 3) {
                    0 -> ShapeType.FILLED_CIRCLE
                    1 -> ShapeType.OUTLINED_CIRCLE
                    else -> ShapeType.RHOMBUS
                }

                val size = when (shapeType) {
                    ShapeType.FILLED_CIRCLE -> 12f + orbitIndex * 0.6f
                    ShapeType.OUTLINED_CIRCLE -> 18f + orbitIndex * 0.8f
                    ShapeType.RHOMBUS -> 14f + orbitIndex * 0.7f
                }

                add(
                    OrbitingShape(
                        color = Color(colors[orbitIndex]),
                        radius = orbitRadius,
                        size = size,
                        speedMultiplier = speedMultiplier,
                        shapeType = shapeType,
                        orbitLineColor = orbitColor,
                        angleOffset = angle
                    )
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 🌌 Galaxy Animation Composable
// -------------------------------------------------------------
@Composable
fun GalaxyAnimation(modifier: Modifier = Modifier) {
    val shapes = remember { createOrbitShapes() }

    // Animate rotation slowly
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(160000, easing = LinearEasing), // super slow
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Orbit center outside top-left
        val orbitCenter = Offset(-size.width * 0.4f, -size.height * 0.4f)

        shapes.forEach { shape ->
            val angle = rotation.value * shape.speedMultiplier + shape.angleOffset
            val rad = Math.toRadians(angle.toDouble())

            // shape position based on orbit radius and angle
            val x = orbitCenter.x + shape.radius * cos(rad).toFloat()
            val y = orbitCenter.y + shape.radius * sin(rad).toFloat()

            // Draw faint orbit line
            drawCircle(
                color = shape.orbitLineColor.copy(alpha = 0.08f),
                radius = shape.radius,
                center = orbitCenter,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )

            // Draw the shape
            when (shape.shapeType) {
                ShapeType.FILLED_CIRCLE -> {
                    drawCircle(
                        color = shape.color,
                        radius = shape.size,
                        center = Offset(x, y)
                    )
                }

                ShapeType.OUTLINED_CIRCLE -> {
                    drawCircle(
                        color = shape.color,
                        radius = shape.size,
                        center = Offset(x, y),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }

                ShapeType.RHOMBUS -> {
                    rotate(angle, Offset(x, y)) {
                        scale(1f, 1f) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(x, y - shape.size)
                                lineTo(x + shape.size, y)
                                lineTo(x, y + shape.size)
                                lineTo(x - shape.size, y)
                                close()
                            }
                            drawPath(path, color = shape.color)
                        }
                    }
                }
            }
        }
    }
}
