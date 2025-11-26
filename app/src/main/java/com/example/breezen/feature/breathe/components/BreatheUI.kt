package com.example.breezen.feature.breathe.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breezen.core.ui.theme.SystemPause
import com.example.breezen.core.ui.theme.SystemStop
import com.example.breezen.feature.breathe.model.BreathingTechnique
import com.example.breezen.feature.breathe.model.RingSpec
import kotlinx.coroutines.delay

@Composable
internal fun DirectionInstruction(
    technique: BreathingTechnique, isPlaying: Boolean
) {
    var currentPhase by remember { mutableStateOf("Ready") }

    LaunchedEffect(isPlaying, technique) {
        if (!isPlaying) {
            currentPhase = "Ready"
            return@LaunchedEffect
        }

        val inhaleMillis = (technique.inhaleTime * 1000L).coerceAtLeast(1)
        val holdMillis = (technique.holdTime * 1000L).coerceAtLeast(0)
        val exhaleMillis = (technique.exhaleTime * 1000L).coerceAtLeast(1)
        val pauseMillis = (technique.pauseTime * 1000L).coerceAtLeast(0)

        while (isPlaying) {
            currentPhase = "Inhale"
            delay(inhaleMillis)
            if (!isPlaying) break

            if (technique.holdTime > 0) {
                currentPhase = "Hold"
                delay(holdMillis)
                if (!isPlaying) break
            }

            currentPhase = "Exhale"
            delay(exhaleMillis)
            if (!isPlaying) break

            if (technique.pauseTime > 0) {
                currentPhase = "Pause"
                delay(pauseMillis)
                if (!isPlaying) break
            }
        }
    }

    if (isPlaying) {
        val directionText = when {
            technique.name == "4-7-8 Breathing" && currentPhase == "Exhale" -> "Through mouth with 'whoosh' sound"
            technique.name == "Double Inhale" && currentPhase == "Inhale" -> "Two quick inhales"
            technique.name == "Double Inhale" && currentPhase == "Exhale" -> "Long slow exhale through mouth"
            technique.name == "Bumblebee Breathing" && currentPhase == "Exhale" -> "Exhale with humming 'mmm' sound"
            technique.name == "Alternate Nostril" -> "Switch nostrils each breath"
            currentPhase == "Inhale" -> "Breathe in through nose"
            currentPhase == "Exhale" -> "Breathe out through mouth"
            currentPhase == "Hold" -> "Hold your breath gently"
            currentPhase == "Pause" -> "Rest with empty lungs"
            else -> ""
        }

        if (directionText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(.7f)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        ), RoundedCornerShape(24.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = directionText,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun BreathingAnimation(
    technique: BreathingTechnique, isPlaying: Boolean, rings: List<RingSpec>
) {
    var currentPhase by remember { mutableStateOf("Ready") }
    var currentRingIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(isPlaying, technique) {
        if (!isPlaying) {
            currentPhase = "Ready"
            currentRingIndex = -1
            return@LaunchedEffect
        }

        // Ensure phase times are not zero to avoid division errors
        val inhaleMillis = (technique.inhaleTime * 1000L).coerceAtLeast(1)
        val holdMillis = (technique.holdTime * 1000L).coerceAtLeast(0)
        val exhaleMillis = (technique.exhaleTime * 1000L).coerceAtLeast(1)
        val pauseMillis = (technique.pauseTime * 1000L).coerceAtLeast(0)

        val inhaleStepMillis = (inhaleMillis / rings.size).coerceAtLeast(1)
        val exhaleStepMillis = (exhaleMillis / rings.size).coerceAtLeast(1)

        while (isPlaying) {
            // Inhale phase
            currentPhase = "Inhale"
            for (i in rings.indices) {
                if (!isPlaying) break
                currentRingIndex = i
                delay(inhaleStepMillis)
            }
            if (!isPlaying) break

            // Hold phase
            if (technique.holdTime > 0) {
                currentPhase = "Hold"
                delay(holdMillis)
                if (!isPlaying) break
            }

            // Exhale phase
            currentPhase = "Exhale"
            for (i in rings.indices.reversed()) {
                if (!isPlaying) break
                currentRingIndex = i - 1
                delay(exhaleStepMillis)
            }
            currentRingIndex = -1
            if (!isPlaying) break

            // Pause phase
            if (technique.pauseTime > 0) {
                currentPhase = "Pause"
                delay(pauseMillis)
                if (!isPlaying) break
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        // Rings
        rings.sortedByDescending { it.size }.forEachIndexed { index, ring ->
            val isVisible = rings.indexOf(ring) <= currentRingIndex

            AnimatedRing(
                ringColor = ring.color, targetSize = ring.size, visible = isVisible && isPlaying
            )
        }

        Text(
            text = currentPhase.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (isPlaying) 1.0f else 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
internal fun EnhancedControlCard(
    selectedTechnique: BreathingTechnique,
    remainingTime: Int,
    totalSessionTime: Int,
    isPlaying: Boolean,
    onTechniqueClick: () -> Unit,
    onTimerClick: () -> Unit,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val currentSeconds = remainingTime % 60
    val currentMinutes = remainingTime / 60

    val animatedSeconds by animateIntAsState(
        targetValue = currentSeconds,
        animationSpec = tween(durationMillis = 300, easing = EaseInOut), // Smoother animation
        label = "seconds"
    )
    val animatedMinutes by animateIntAsState(
        targetValue = currentMinutes,
        animationSpec = tween(durationMillis = 300, easing = EaseInOut),
        label = "minutes"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface, // Use theme "surface"
                    RoundedCornerShape(28.dp)
                )
                .border(
                    1.5.dp,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    RoundedCornerShape(28.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Timer section
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                        ) { onTimerClick() }
                        .padding(start = 10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Second display
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = String.format("%02d", animatedSeconds),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 70.sp),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .alignByBaseline()
                                    .align(Alignment.Top)
                            )
                            Text(
                                text = "s",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .alignByBaseline()
                                    .padding(start = 4.dp)
                            )
                        }

                        // Minute display
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = String.format("%02d", animatedMinutes),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 70.sp),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .alignByBaseline()
                                    .align(Alignment.Top)
                            )
                            Text(
                                text = "m",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .alignByBaseline()
                                    .padding(start = 4.dp)
                            )
                        }
                    }
                }

                // Control section
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Technique selector
                    Card(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                        ) { onTechniqueClick() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Style",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(),
                                    ) { onTechniqueClick() },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = selectedTechnique.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    // Control buttons
                    if (!isPlaying) {
                        // Full width start button
                        Card(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(),
                                ) { onStartClick() },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            )
                                        ),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                            )
                                        ),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Start",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    } else {
                        // Two buttons when playing
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Pause button (Uses SystemPause color)
                            Card(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(),
                                    ) { onPauseClick() },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    SystemPause.copy(alpha = 0.3f),
                                                    SystemPause.copy(alpha = 0.1f)
                                                )
                                            ),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    SystemPause.copy(alpha = 0.4f),
                                                    SystemPause.copy(alpha = 0.2f)
                                                )
                                            ),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Stop button (Uses SystemStop color)
                            Card(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple()
                                    ) { onStopClick() },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    SystemStop.copy(alpha = 0.3f),
                                                    SystemStop.copy(alpha = 0.1f)
                                                )
                                            ),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    SystemStop.copy(alpha = 0.4f),
                                                    SystemStop.copy(alpha = 0.2f)
                                                )
                                            ),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AnimatedRing(
    ringColor: Color, targetSize: Dp, visible: Boolean
) {
    val animatedSize by animateDpAsState(
        targetValue = if (visible) targetSize else 0.dp, animationSpec = tween(
            durationMillis = 600, easing = FastOutSlowInEasing
        ), label = "ring_animation"
    )

    val glowAnimation by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.7f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut), repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    Box(
        modifier = Modifier
            .size(animatedSize)
            .background(
                ringColor.copy(alpha = ringColor.alpha * glowAnimation), shape = CircleShape
            )
    )
}