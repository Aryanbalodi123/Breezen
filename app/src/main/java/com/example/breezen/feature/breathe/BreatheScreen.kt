package com.example.breezen.feature.breathe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.core.ui.components.BackButton
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.BrandGreen
import com.example.breezen.core.ui.theme.TextPrimary
import com.example.breezen.core.ui.theme.TextSecondary
import com.example.breezen.feature.breathe.components.BreathingControlCard
import com.example.breezen.feature.breathe.components.BreathingDirectionText
import com.example.breezen.feature.breathe.components.BreathingRingsAnimation
import com.example.breezen.feature.breathe.components.TechniqueInfoDialog
import com.example.breezen.feature.breathe.components.TechniqueSelectorDialog
import com.example.breezen.feature.breathe.components.TimerSettingDialog
import com.example.breezen.feature.breathe.data.breathingTechniques
import com.example.breezen.feature.breathe.model.BreathingTechnique
import com.example.breezen.feature.breathe.model.RingSpec
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BreatheScreen(navController: NavController) {

    // Haze / blur state (used by dialogs to blur underlying content)
    val hazeState = rememberHazeState()

    // Animation States for Entry
    val animAlpha = remember { Animatable(0f) }
    val animScale = remember { Animatable(0.92f) }
    val animOffsetY = remember { Animatable(100f) } // Slide up distance in px (approx)
    val animTopBarOffset = remember { Animatable(-50f) }

    // Trigger Entry Animation
    LaunchedEffect(Unit) {
        // Parallel animations with slight staggers for "sleek" feel
        launch {
            animAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = EaseOutQuart)
            )
        }
        launch {
            // Slight delay for center content bloom
            delay(50)
            animScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = EaseOutQuart)
            )
        }
        launch {
            // Controls slide up
            delay(100)
            animOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 900, easing = EaseOutQuart)
            )
        }
        launch {
            // Top bar slide down
            animTopBarOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 700, easing = EaseOutQuart)
            )
        }
    }

    // rings use BrandGreen with decreasing alpha values (brand-based look)
    val rings = listOf(
        RingSpec(60.dp, BrandGreen.copy(alpha = 0.70f)),
        RingSpec(120.dp, BrandGreen.copy(alpha = 0.60f)),
        RingSpec(180.dp, BrandGreen.copy(alpha = 0.50f)),
        RingSpec(240.dp, BrandGreen.copy(alpha = 0.40f))
    )

    var selectedTechnique by remember { mutableStateOf(breathingTechniques[0]) }
    var showTechniqueSelector by remember { mutableStateOf(false) }
    var showTechniqueInfo by remember { mutableStateOf<BreathingTechnique?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var totalSessionTime by remember { mutableIntStateOf(300) }
    var remainingTime by remember { mutableIntStateOf(totalSessionTime) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // Timer tick
    LaunchedEffect(isPlaying, remainingTime) {
        if (!isPlaying) return@LaunchedEffect
        while (isPlaying && remainingTime > 0) {
            delay(1000)
            remainingTime--
        }
        if (remainingTime <= 0) {
            isPlaying = false
            remainingTime = totalSessionTime
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBlack)
    ) {
        // ---------- CONTENT LAYER (blurrable) ----------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState) // mark content as blur-source
        ) {
            // Ambient lights (top-left and bottom-right)
            // Applied entry fade to background elements
            Box(modifier = Modifier.graphicsLayer { alpha = animAlpha.value }) {
                AmbientLight(
                    color = BrandGreen.copy(alpha = 0.25f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 50.dp, start = 20.dp)
                )
                AmbientLight(
                    color = BrandGreen.copy(alpha = 0.18f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 100.dp, end = 20.dp)
                )
            }

            // Main UI column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top bar with back button and title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .graphicsLayer {
                            translationY = animTopBarOffset.value
                            alpha = animAlpha.value
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BackButton(navController = navController)

                    Text(
                        text = "Breathe",
                        style = AppTypography.headlineMedium,
                        color = TextPrimary
                    )

                    // placeholder to keep title centered
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Center content (either intro text when idle, or animation when playing)
                // Applied Scale + Fade Bloom Animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = animScale.value
                            scaleY = animScale.value
                            alpha = animAlpha.value
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!isPlaying) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = selectedTechnique.name,
                                style = AppTypography.displayLarge,
                                color = BrandGreen,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Find a comfortable position.\nPrepare to relax your mind.",
                                style = AppTypography.bodyLarge,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = "Ready?",
                                style = AppTypography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Light),
                                color = TextSecondary.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        BreathingRingsAnimation(
                            technique = selectedTechnique,
                            isPlaying = true,
                            rings = rings
                        )
                    }
                }

                // Direction / short instructions
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .graphicsLayer { alpha = animAlpha.value }
                ) {
                    BreathingDirectionText(technique = selectedTechnique, isPlaying = isPlaying)
                }

                // Controls
                // Applied Slide Up + Fade Animation
                Box(
                    modifier = Modifier.graphicsLayer {
                        translationY = animOffsetY.value
                        alpha = animAlpha.value
                    }
                ) {
                    BreathingControlCard(
                        selectedTechnique = selectedTechnique,
                        remainingTime = remainingTime,
                        isPlaying = isPlaying,
                        onTechniqueClick = { showTechniqueSelector = true },
                        onTimerClick = { showTimerDialog = true },
                        onStartClick = {
                            if (remainingTime == 0) remainingTime = totalSessionTime
                            isPlaying = true
                        },
                        onPauseClick = { isPlaying = false },
                        onStopClick = {
                            isPlaying = false
                            remainingTime = totalSessionTime
                        }
                    )
                }

                Spacer(modifier = Modifier.height(150.dp))
            }
        }

        // ---------- OVERLAY LAYER (dialogs that blur content) ----------
        AnimatedVisibility(visible = showTechniqueSelector, enter = fadeIn(), exit = fadeOut()) {
            TechniqueSelectorDialog(
                hazeState = hazeState,
                techniques = breathingTechniques,
                selectedTechnique = selectedTechnique,
                onTechniqueSelected = {
                    selectedTechnique = it
                    showTechniqueSelector = false
                    isPlaying = false
                    remainingTime = totalSessionTime
                },
                onInfoClick = { showTechniqueInfo = it },
                onDismiss = { showTechniqueSelector = false }
            )
        }

        AnimatedVisibility(visible = showTechniqueInfo != null, enter = fadeIn(), exit = fadeOut()) {
            showTechniqueInfo?.let { technique ->
                TechniqueInfoDialog(
                    hazeState = hazeState,
                    technique = technique,
                    onDismiss = { showTechniqueInfo = null }
                )
            }
        }

        AnimatedVisibility(visible = showTimerDialog, enter = fadeIn(), exit = fadeOut()) {
            TimerSettingDialog(
                hazeState = hazeState,
                currentTime = totalSessionTime,
                onTimeSet = {
                    totalSessionTime = it
                    remainingTime = it
                    showTimerDialog = false
                    isPlaying = false
                },
                onDismiss = { showTimerDialog = false }
            )
        }
    }
}

@Composable
private fun AmbientLight(color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(300.dp)
            .blur(80.dp)
            .background(
                Brush.radialGradient(
                    listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0.05f), androidx.compose.ui.graphics.Color.Transparent)
                ),
                CircleShape
            )
    )
}