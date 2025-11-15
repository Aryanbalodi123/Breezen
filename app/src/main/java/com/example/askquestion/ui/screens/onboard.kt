package com.example.askquestion.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.askquestion.theme.CustomTypography
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
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

enum class OnboardingState { WELCOME, SIGN_UP, SIGN_IN }


// --- Main Onboarding Screen ---

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OnboardingScreen(
    navController: NavController,
    onOnboardingComplete: () -> Unit // This is the success callback
) {

    var state by remember { mutableStateOf(OnboardingState.WELCOME) }
    val revealRadius = remember { Animatable(0f) }
    // Changed button color to green theme
    val buttonColor = Color(0xFF00C853)
    var hasRevealed by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        val density = LocalDensity.current

        // --- START OF GLASS EFFECT ANIMATIONS ---

        // Animate the blur radius based on the current state
        val blurAmount by animateDpAsState(
            targetValue = if (state == OnboardingState.WELCOME) 0.dp else 16.dp,
            label = "blurAnimation",
            animationSpec = tween(durationMillis = 700)
        )

        // Animate the alpha (dimness) based on the state
        val alphaAmount by animateFloatAsState(
            targetValue = if (state == OnboardingState.WELCOME) 1.0f else 0.4f, // 0.4f = "very very little"
            label = "alphaAnimation",
            animationSpec = tween(durationMillis = 700)
        )
        // --- END OF GLASS EFFECT ANIMATIONS ---

        val shapes = remember { createOrbitShapes() }

        // 1. The background animation (always running)
        GalaxyAnimation(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = blurAmount) // Apply blur
                .alpha(alphaAmount), // Apply dimness
            shapes = shapes
        )

        // 2. The Sign Up content
        AnimatedVisibility(
            visible = state == OnboardingState.SIGN_UP,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000, delayMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            SignUpScreen(
                onSignInClick = { state = OnboardingState.SIGN_IN }, // Go to Sign In
                onSignUpSuccess = onOnboardingComplete // *** UPDATED ***
            )
        }

        // 3. The Sign In content
        AnimatedVisibility(
            visible = state == OnboardingState.SIGN_IN,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000, delayMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            SignInScreen(
                onSignUpClick = { state = OnboardingState.SIGN_UP }, // Go to Sign Up
                onSignInSuccess = onOnboardingComplete // *** UPDATED ***
            )
        }

        // 4. The Welcome content
        AnimatedVisibility(
            visible = state == OnboardingState.WELCOME,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            WelcomeContent(
                onGetStartedClick = {
                    // Start the transition to the SIGN_UP state
                    state = OnboardingState.SIGN_UP
                }
            )
        }

        // 5. The reveal "flash" circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = buttonColor,
                radius = revealRadius.value,
                center = Offset(x = size.width / 2f, y = size.height - 100.dp.toPx())
            )
        }

        // 6. The animation driver
        LaunchedEffect(state) {
            // Only run the reveal animation ONCE when moving away from WELCOME
            if (state != OnboardingState.WELCOME && !hasRevealed) {
                hasRevealed = true // Mark as revealed
                val widthInPx = with(density) { maxWidth.toPx() }
                val heightInPx = with(density) { maxHeight.toPx() }

                val maxRadius = (sqrt(widthInPx.pow(2) + heightInPx.pow(2)) * 1.2f).toFloat()

                revealRadius.animateTo(
                    targetValue = maxRadius,
                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
                )

                revealRadius.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 500, delayMillis = 300)
                )
            }
        }
    }
}

// --- UI Components ---

@Composable
private fun WelcomeContent(onGetStartedClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "Welcome to Breezen",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = CustomTypography.displayLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Find your calm. Start your journey to mindfulness.",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = CustomTypography.bodyMedium
        )

        Spacer(modifier = Modifier.height(64.dp))

        // "Breathing" Button
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "button_scale"
        )
        // Changed button to green gradient
        val meditationBrush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF00C853), Color(0xFF69F0AE))
        )

        Button(
            onClick = { onGetStartedClick() },
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .height(70.dp)
                .fillMaxWidth(0.8f),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(meditationBrush),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Continue Journey",
                    color = Color.Black, // Changed text to black for contrast
                    style = CustomTypography.bodyLarge.copy(fontSize = 18.sp),
                    maxLines = 1
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}


// --- Galaxy Animation Code (Unchanged) ---
// (No changes needed to the animation code below)

private fun createOrbitShapes(): List<OrbitingShape> {
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
            drawOrbitingShape(shape, angle, centerX, centerY)
        }
    }
}

fun DrawScope.drawOrbitLine(color: Color, radius: Float, centerX: Float, centerY: Float) {
    drawOval(
        color = color,
        topLeft = Offset(centerX - radius, centerY - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 1.dp.toPx())
    )
}

fun DrawScope.drawOrbitingShape(
    shape: OrbitingShape,
    angle: Float,
    centerX: Float,
    centerY: Float
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
            drawCircle(color = Color.Black, radius = shape.size, center = Offset(x, y))
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
            drawPath(path, color = Color.Black)
            drawPath(path, color = shape.color, style = Stroke(width = 2.5.dp.toPx()))
        }
    }
}