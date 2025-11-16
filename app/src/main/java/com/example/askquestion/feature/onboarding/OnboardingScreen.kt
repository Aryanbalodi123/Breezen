package com.example.askquestion.feature.onboarding

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.askquestion.core.ui.theme.FunnelDisplayFamily
import com.example.askquestion.core.ui.theme.InstrumentalSerifFamily
import com.example.askquestion.feature.onboarding.components.GalaxyAnimation
import com.example.askquestion.feature.onboarding.components.createOrbitShapes
import kotlin.math.pow
import kotlin.math.sqrt

// --- Enums and Data Classes ---

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
    var hasRevealed by remember { mutableStateOf(false) }

    // Get theme colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val buttonColor = MaterialTheme.colorScheme.secondary

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        val density = LocalDensity.current

        // --- START OF GLASS EFFECT ANIMATIONS ---

        val blurAmount by animateDpAsState(
            targetValue = if (state == OnboardingState.WELCOME) 0.dp else 16.dp,
            label = "blurAnimation",
            animationSpec = tween(durationMillis = 700)
        )

        val alphaAmount by animateFloatAsState(
            targetValue = if (state == OnboardingState.WELCOME) 1.0f else 0.4f,
            label = "alphaAnimation",
            animationSpec = tween(durationMillis = 700)
        )
        // --- END OF GLASS EFFECT ANIMATIONS ---

        val shapes = remember { createOrbitShapes() }

        // 1. The background animation (always running)
        GalaxyAnimation(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = blurAmount)
                .alpha(alphaAmount),
            shapes = shapes
        )

        // 2. The Sign Up content
        AnimatedVisibility(
            visible = state == OnboardingState.SIGN_UP,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000, delayMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            SignUpScreen(
                onSignInClick = { state = OnboardingState.SIGN_IN },
                onSignUpSuccess = onOnboardingComplete
            )
        }

        // 3. The Sign In content
        AnimatedVisibility(
            visible = state == OnboardingState.SIGN_IN,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000, delayMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            SignInScreen(
                onSignUpClick = { state = OnboardingState.SIGN_UP },
                onSignInSuccess = onOnboardingComplete
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
            if (state != OnboardingState.WELCOME && !hasRevealed) {
                hasRevealed = true
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
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayLarge,
            fontFamily = FunnelDisplayFamily // Apply creative font
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Find your calm. Start your journey to mindfulness.",
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = InstrumentalSerifFamily // Apply creative font
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

        val meditationBrush = Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.primary
            )
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
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    maxLines = 1
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}