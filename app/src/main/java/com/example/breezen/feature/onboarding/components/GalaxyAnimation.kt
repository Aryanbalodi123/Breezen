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
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.BlackAlpha40
import com.example.breezen.core.ui.theme.BlackAlpha60
import com.example.breezen.core.ui.theme.BlackAlpha80
import com.example.breezen.core.ui.theme.BlackAlpha90
import com.example.breezen.core.ui.theme.SolidBlack
import com.example.breezen.core.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// -----------------------------
//  ENUMS & DATA STRUCTURES
// -----------------------------

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

// -----------------------------
//  ORBIT SHAPE CREATION
// -----------------------------

internal fun createOrbitShapes(): List<OrbitingShape> {

    // Predefined grayscale palette mapped from your color.kt
    val shapeColors = listOf(
        SolidBlack,
        AppBlack,
        BlackAlpha90,
        BlackAlpha80,
        BlackAlpha60,
        BlackAlpha40,
        TextSecondary,
        AppWhite.copy(alpha = 0.70f),
        AppWhite.copy(alpha = 0.80f),
        AppWhite.copy(alpha = 0.90f),
        AppWhite,
    )

    val orbitColors = listOf(
        SolidBlack,
        AppBlack,
        BlackAlpha90,
        BlackAlpha80,
        BlackAlpha60,
        BlackAlpha40,
        TextSecondary,
        AppWhite.copy(alpha = 0.70f),
        AppWhite.copy(alpha = 0.80f),
        AppWhite.copy(alpha = 0.90f),
        AppWhite.copy(alpha = 0.95f)
    )

    val radii = listOf(
        150f, 210f, 279f, 358f, 449f, 554f,
        675f, 814f, 974f, 1158f, 1370f, 1614f
    )

    val speeds = listOf(
        1.0f, 0.91f, 0.82f, 0.73f, 0.64f, 0.55f,
        0.46f, 0.37f, 0.28f, 0.19f, 0.12f, 0.06f
    )

    val shapesPerOrbit = listOf(
        4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15
    )

    return buildList {
        for (orbitIndex in radii.indices) {

            val numShapes = shapesPerOrbit[orbitIndex]
            val angleIncrement = 360f / numShapes
            val orbitDirection = if (Random.nextBoolean()) 1 else -1

            for (shapeIndex in 0 until numShapes) {

                val baseColor = shapeColors[orbitIndex % shapeColors.size]
                val orbitColor = orbitColors[orbitIndex % orbitColors.size]
                val angle = angleIncrement * shapeIndex

                val shapeType = ShapeType.entries.toTypedArray().random()

                val sizeFactor = Random.nextFloat() * 0.4f + 0.8f
                val size = when (shapeType) {
                    ShapeType.FILLED_CIRCLE -> (14f + orbitIndex * 0.4f) * sizeFactor
                    ShapeType.OUTLINED_CIRCLE -> (20f + orbitIndex * 0.8f) * sizeFactor
                    ShapeType.RHOMBUS -> (18f + orbitIndex * 0.6f) * sizeFactor
                }

                val speedFactor = Random.nextFloat() * 0.3f + 0.85f

                add(
                    OrbitingShape(
                        color = baseColor,
                        radius = radii[orbitIndex],
                        size = size,
                        speedMultiplier = speeds[orbitIndex] * speedFactor,
                        shapeType = shapeType,
                        orbitLineColor = orbitColor,
                        direction = orbitDirection,
                        angleOffset = angle
                    )
                )
            }
        }
    }
}

// -----------------------------
//  DRAWING ENGINE
// -----------------------------

@Composable
fun GalaxyAnimation(modifier: Modifier = Modifier, shapes: List<OrbitingShape>) {

    val infiniteTransition = rememberInfiniteTransition(label = "galaxy")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(90000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    val backgroundColor = MaterialTheme.colorScheme.background

    Canvas(modifier = modifier) {

        val centerX = -size.width * 0.2f
        val centerY = -size.height * 0.2f

        val uniqueRadii = shapes.map { it.radius }.distinct().sorted()

        uniqueRadii.forEach { r ->
            val orbitCol = shapes.first { it.radius == r }.orbitLineColor
            drawOrbitLine(orbitCol.copy(alpha = 0.5f), r, centerX, centerY)
        }

        shapes.forEach { shape ->
            drawOrbitingShape(shape, angle, centerX, centerY, backgroundColor)
        }
    }
}

internal fun DrawScope.drawOrbitLine(
    color: Color,
    radius: Float,
    cx: Float,
    cy: Float
) {
    drawOval(
        color = color,
        topLeft = Offset(cx - radius, cy - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 1.dp.toPx())
    )
}

internal fun DrawScope.drawOrbitingShape(
    shape: OrbitingShape,
    angle: Float,
    cx: Float,
    cy: Float,
    backgroundColor: Color
) {
    val rotated = Math.toRadians(
        ((angle * shape.speedMultiplier * shape.direction) + shape.angleOffset).toDouble()
    )

    val x = cx + shape.radius * cos(rotated).toFloat()
    val y = cy + shape.radius * sin(rotated).toFloat()

    when (shape.shapeType) {

        ShapeType.FILLED_CIRCLE -> {
            drawCircle(shape.color, radius = shape.size, center = Offset(x, y))
        }

        ShapeType.OUTLINED_CIRCLE -> {
            drawCircle(backgroundColor, radius = shape.size, center = Offset(x, y))
            drawCircle(
                shape.color,
                radius = shape.size,
                center = Offset(x, y),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        ShapeType.RHOMBUS -> {
            val hw = shape.size * 0.7f
            val hh = shape.size * 1.4f

            val path = Path().apply {
                moveTo(x, y - hh)
                lineTo(x + hw, y)
                lineTo(x, y + hh)
                lineTo(x - hw, y)
                close()
            }

            drawPath(path, color = backgroundColor)
            drawPath(path, color = shape.color, style = Stroke(width = 2.5.dp.toPx()))
        }
    }
}
