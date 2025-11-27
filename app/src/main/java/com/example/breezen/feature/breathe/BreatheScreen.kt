package com.example.breezen.feature.breathe

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.core.ui.components.BackButton
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.feature.breathe.components.BreathingAnimation
import com.example.breezen.feature.breathe.components.DirectionInstruction
import com.example.breezen.feature.breathe.components.EnhancedControlCard
import com.example.breezen.feature.breathe.components.TechniqueInfoDialog
import com.example.breezen.feature.breathe.components.TechniqueSelectorDialog
import com.example.breezen.feature.breathe.components.TimerSettingDialog
import com.example.breezen.feature.breathe.data.breathingTechniques
import com.example.breezen.feature.breathe.model.BreathingTechnique
import com.example.breezen.feature.breathe.model.RingSpec
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.delay

@Composable
fun BreatheScreen(navController: NavController) {

    // --- 1. Setup Haze State ---
    val hazeState = remember { HazeState() }

    val ambientColor = MaterialTheme.colorScheme.primary

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
    var totalSessionTime by remember { mutableStateOf(300) }
    var remainingTime by remember { mutableStateOf(totalSessionTime) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // Timer Logic
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
            .background(Color.Black)
    ) {
        // --- CONTENT LAYER (This gets blurred) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState) // Mark this content as "blurrable"
        ) {
            // Ambient Background Lights
            AmbientLight(
                color = ambientColor,
                modifier = Modifier.align(Alignment.TopStart).padding(top = 50.dp, start = 20.dp)
            )
            AmbientLight(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 100.dp, end = 20.dp)
            )

            // Main UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.CenterStart) {
                        BackButton(navController)
                    }
                    Text(
                        text = "Breathe",
                        style = AppTypography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Box(modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))

                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (!isPlaying) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = selectedTechnique.name,
                                style = AppTypography.displayLarge,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Find a comfortable position.\nPrepare to relax your mind.",
                                style = AppTypography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Ready?",
                                style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Light),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        BreathingAnimation(
                            technique = selectedTechnique,
                            isPlaying = isPlaying,
                            rings = rings
                        )
                    }
                }

                Box(modifier = Modifier.padding(vertical = 10.dp)) {
                    DirectionInstruction(technique = selectedTechnique, isPlaying = isPlaying)
                }

                EnhancedControlCard(
                    selectedTechnique = selectedTechnique,
                    remainingTime = remainingTime,
                    totalSessionTime = totalSessionTime,
                    isPlaying = isPlaying,
                    onTechniqueClick = { showTechniqueSelector = true },
                    onTimerClick = { showTimerDialog = true },
                    onStartClick = {
                        if (remainingTime == 0) remainingTime = totalSessionTime
                        isPlaying = true
                    },
                    onPauseClick = { isPlaying = false },
                    onStopClick = { isPlaying = false; remainingTime = totalSessionTime }
                )
                Spacer(modifier = Modifier.height(150.dp))
            }
        }

        // --- OVERLAY LAYER (The Glass Dialogs) ---
        // These are outside the .haze() container but reference hazeState to blur it

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
private fun AmbientLight(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(300.dp)
            .blur(80.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0.05f), Color.Transparent)
                ),
                CircleShape
            )
    )
}