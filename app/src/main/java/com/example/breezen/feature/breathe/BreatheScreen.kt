package com.example.breezen.feature.breathe

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.feature.breathe.components.BreathingAnimation
import com.example.breezen.feature.breathe.components.DirectionInstruction
import com.example.breezen.feature.breathe.components.EnhancedControlCard
import com.example.breezen.feature.breathe.components.TechniqueInfoDialog
import com.example.breezen.feature.breathe.components.TechniqueSelectorDialog
import com.example.breezen.feature.breathe.components.TimerSettingDialog
import com.example.breezen.feature.breathe.data.breathingTechniques
import com.example.breezen.feature.breathe.model.BreathingTechnique
import com.example.breezen.feature.breathe.model.RingSpec
import kotlinx.coroutines.delay

// --- DELETED hardcoded colors ---
// val accentGreen = ...
// val accentGreenLight = ...
// val accentGreenDark = ...

@Composable
fun BreatheScreen(navController: NavController) {

    // --- USE THEME COLORS ---
    val darkGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    // Ring colors now use your app's PRIMARY green
    val rings = listOf(
        RingSpec(60.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
        RingSpec(120.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
        RingSpec(180.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        RingSpec(240.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    )

    var selectedTechnique by remember { mutableStateOf(breathingTechniques[0]) }
    var showTechniqueSelector by remember { mutableStateOf(false) }
    var showTechniqueInfo by remember { mutableStateOf<BreathingTechnique?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var totalSessionTime by remember { mutableStateOf(300) } // 5 minutes default
    var remainingTime by remember { mutableStateOf(totalSessionTime) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // Timer effect - countdown
    LaunchedEffect(isPlaying, remainingTime) { // added remainingTime to ensure coroutine resets
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

    val pulseAnimation by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.3f, targetValue = 0.7f, animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut), repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkGradient) // Use theme gradient
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(pulseAnimation * 0.1f)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size((200 + index * 100).dp)
                        .offset(
                            x = (50 + index * 150).dp, y = (100 + index * 200).dp
                        )
                        .background(
                            // Use theme primary green
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f), CircleShape
                        )
                        .blur(20.dp)
                )
            }
        }

        // Back button (Glassy style)
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, bottom = 100.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Breathing animation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                BreathingAnimation(
                    technique = selectedTechnique, isPlaying = isPlaying, rings = rings
                )
            }
            Box(
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                DirectionInstruction(
                    technique = selectedTechnique, isPlaying = isPlaying
                )
            }

            // Enhanced control card
            EnhancedControlCard(
                selectedTechnique = selectedTechnique,
                remainingTime = remainingTime,
                totalSessionTime = totalSessionTime,
                isPlaying = isPlaying,
                onTechniqueClick = { showTechniqueSelector = true },
                onTimerClick = { showTimerDialog = true },
                onStartClick = {
                    if (remainingTime == 0) remainingTime = totalSessionTime // Reset if stopped at 0
                    isPlaying = true
                },
                onPauseClick = {
                    isPlaying = false
                },
                onStopClick = {
                    isPlaying = false
                    remainingTime = totalSessionTime
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Technique selector dialog
        if (showTechniqueSelector) {
            TechniqueSelectorDialog(
                techniques = breathingTechniques,
                selectedTechnique = selectedTechnique,
                onTechniqueSelected = { technique ->
                    selectedTechnique = technique
                    showTechniqueSelector = false
                    isPlaying = false
                    remainingTime = totalSessionTime
                },
                onInfoClick = { technique ->
                    showTechniqueInfo = technique
                },
                onDismiss = { showTechniqueSelector = false })
        }

        // Technique info dialog
        showTechniqueInfo?.let { technique ->
            TechniqueInfoDialog(technique = technique, onDismiss = { showTechniqueInfo = null })
        }

        // Timer setting dialog
        if (showTimerDialog) {
            TimerSettingDialog(currentTime = totalSessionTime, onTimeSet = { newTime ->
                totalSessionTime = newTime
                remainingTime = newTime
                showTimerDialog = false
                isPlaying = false // Stop session when new time is set
            }, onDismiss = { showTimerDialog = false })
        }
    }
}