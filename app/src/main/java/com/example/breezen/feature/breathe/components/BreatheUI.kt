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
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.BrandGreen
import com.example.breezen.core.ui.theme.CornerMedium
import com.example.breezen.core.ui.theme.CornerSmall
import com.example.breezen.core.ui.theme.CornerXLarge
import com.example.breezen.core.ui.theme.SystemPause
import com.example.breezen.core.ui.theme.SystemStop
import com.example.breezen.core.ui.theme.TextPrimary
import com.example.breezen.core.ui.theme.TextSecondary
import com.example.breezen.core.ui.theme.WhiteAlpha03
import com.example.breezen.core.ui.theme.WhiteAlpha06
import com.example.breezen.core.ui.theme.WhiteAlpha12
import com.example.breezen.feature.breathe.model.BreathingTechnique
import com.example.breezen.feature.breathe.model.RingSpec
import kotlinx.coroutines.delay

// ------- Breathing direction text -------
// ------- Purpose: show short guidance for current phase -------
@Composable
internal fun BreathingDirectionText(
    technique: BreathingTechnique,
    isPlaying: Boolean
) {
    var currentPhase by remember { mutableStateOf("Ready") }

    LaunchedEffect(isPlaying, technique) {
        if (!isPlaying) {
            currentPhase = "Ready"
            return@LaunchedEffect
        }

        val inhaleMillis = (technique.inhaleTime * 1000L).coerceAtLeast(1)
        val holdMillis = (technique.holdTime * 1000L)
        val exhaleMillis = (technique.exhaleTime * 1000L).coerceAtLeast(1)
        val pauseMillis = (technique.pauseTime * 1000L)

        while (true) {
            currentPhase = "Inhale"
            delay(inhaleMillis)

            if (technique.holdTime > 0) {
                currentPhase = "Hold"
                delay(holdMillis)
            }

            currentPhase = "Exhale"
            delay(exhaleMillis)

            if (technique.pauseTime > 0) {
                currentPhase = "Pause"
                delay(pauseMillis)
            }
        }
    }

    if (!isPlaying) return

    val directionText = when {
        technique.name == "4-7-8 Breathing" && currentPhase == "Exhale" ->
            "Through mouth with 'whoosh' sound"
        technique.name == "Double Inhale" && currentPhase == "Inhale" ->
            "Two quick inhales"
        technique.name == "Double Inhale" && currentPhase == "Exhale" ->
            "Long slow exhale through mouth"
        technique.name == "Bumblebee Breathing" && currentPhase == "Exhale" ->
            "Exhale with humming 'mmm' sound"
        technique.name == "Alternate Nostril" ->
            "Switch nostrils each breath"
        currentPhase == "Inhale" -> "Breathe in through nose"
        currentPhase == "Exhale" -> "Breathe out through mouth"
        currentPhase == "Hold" -> "Hold your breath gently"
        currentPhase == "Pause" -> "Rest with empty lungs"
        else -> ""
    }

    if (directionText.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxWidth(.7f)
            .background(
                Brush.horizontalGradient(
                    listOf(BrandGreen.copy(alpha = 0.30f), BrandGreen.copy(alpha = 0.20f))
                ),
                RoundedCornerShape(CornerMedium)
            )
            .border(1.dp, BrandGreen.copy(alpha = 0.40f), RoundedCornerShape(CornerMedium))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = directionText,
            style = AppTypography.titleMedium.copy(fontSize = 16.sp),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

// ------- Breathing rings animation -------
// ------- Purpose: animated rings representing breath phases -------
@Composable
internal fun BreathingRingsAnimation(
    technique: BreathingTechnique,
    isPlaying: Boolean,
    rings: List<RingSpec>
) {
    var currentPhase by remember { mutableStateOf("Ready") }
    var currentRingIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(isPlaying, technique) {
        if (!isPlaying) {
            currentPhase = "Ready"
            currentRingIndex = -1
            return@LaunchedEffect
        }

        val inhaleMillis = (technique.inhaleTime * 1000L).coerceAtLeast(1)
        val holdMillis = (technique.holdTime * 1000L)
        val exhaleMillis = (technique.exhaleTime * 1000L).coerceAtLeast(1)
        val pauseMillis = (technique.pauseTime * 1000L)

        val inhaleStep = (inhaleMillis / rings.size).coerceAtLeast(1)
        val exhaleStep = (exhaleMillis / rings.size).coerceAtLeast(1)

        while (true) {
            currentPhase = "Inhale"
            for (i in rings.indices) {
                currentRingIndex = i
                delay(inhaleStep)
            }

            if (technique.holdTime > 0) {
                currentPhase = "Hold"
                delay(holdMillis)
            }

            currentPhase = "Exhale"
            for (i in rings.indices.reversed()) {
                currentRingIndex = i - 1
                delay(exhaleStep)
            }
            currentRingIndex = -1

            if (technique.pauseTime > 0) {
                currentPhase = "Pause"
                delay(pauseMillis)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        rings.sortedByDescending { it.size }.forEach { ring ->
            val ringVisible = rings.indexOf(ring) <= currentRingIndex
            BreathingAnimatedRing(
                ringColor = ring.color,
                targetSize = ring.size,
                visible = ringVisible && isPlaying
            )
        }

        Text(
            text = currentPhase.uppercase(),
            style = AppTypography.titleLarge.copy(fontSize = 20.sp),
            color = TextPrimary.copy(alpha = if (isPlaying) 1f else 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

// ------- Breathing control card -------
// ------- Purpose: timer, technique selector, and play controls -------
@SuppressLint("DefaultLocale")
@Composable
internal fun BreathingControlCard(
    selectedTechnique: BreathingTechnique,
    remainingTime: Int,
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
        animationSpec = tween(300, easing = EaseInOut),
        label = "seconds"
    )
    val animatedMinutes by animateIntAsState(
        targetValue = currentMinutes,
        animationSpec = tween(300, easing = EaseInOut),
        label = "minutes"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(CornerXLarge),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(WhiteAlpha03, RoundedCornerShape(CornerXLarge))
                .border(1.dp, WhiteAlpha12, RoundedCornerShape(CornerXLarge))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // timer section
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        ) { onTimerClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%02d", animatedSeconds),
                            style = AppTypography.displayLarge.copy(fontSize = 70.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = "s",
                            style = AppTypography.bodyMedium.copy(fontSize = 18.sp),
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%02d", animatedMinutes),
                            style = AppTypography.displayLarge.copy(fontSize = 70.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = "m",
                            style = AppTypography.bodyMedium.copy(fontSize = 18.sp),
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // controls section
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // technique selector
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        ) { onTechniqueClick() },
                        colors = CardDefaults.cardColors(containerColor = WhiteAlpha12),
                        shape = RoundedCornerShape(CornerMedium)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Style",
                                style = AppTypography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                color = TextPrimary
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple()
                                    ) { onTechniqueClick() },
                                colors = CardDefaults.cardColors(containerColor = WhiteAlpha06),
                                shape = RoundedCornerShape(CornerSmall)
                            ) {
                                Text(
                                    text = selectedTechnique.name,
                                    style = AppTypography.bodySmall,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    // play/pause/stop
                    if (!isPlaying) {
                        Card(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple()
                                ) { onStartClick() },
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(CornerMedium)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            listOf(BrandGreen.copy(alpha = 0.40f), BrandGreen.copy(alpha = 0.25f))
                                        ),
                                        RoundedCornerShape(CornerMedium)
                                    )
                                    .border(1.dp, BrandGreen.copy(alpha = 0.40f), RoundedCornerShape(CornerMedium))
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Start",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Start",
                                        style = AppTypography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // pause
                            Card(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple()
                                    ) { onPauseClick() },
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(CornerMedium)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Brush.linearGradient(
                                                listOf(SystemPause.copy(alpha = 0.30f), SystemPause.copy(alpha = 0.15f))
                                            ),
                                            RoundedCornerShape(CornerMedium)
                                        )
                                        .border(1.dp, SystemPause.copy(alpha = 0.40f), RoundedCornerShape(CornerMedium))
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // stop
                            Card(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple()
                                    ) { onStopClick() },
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(CornerMedium)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Brush.linearGradient(
                                                listOf(SystemStop.copy(alpha = 0.30f), SystemStop.copy(alpha = 0.15f))
                                            ),
                                            RoundedCornerShape(CornerMedium)
                                        )
                                        .border(1.dp, SystemStop.copy(alpha = 0.40f), RoundedCornerShape(CornerMedium))
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Stop,
                                        contentDescription = "Stop",
                                        tint = TextPrimary,
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

// ------- small animated ring -------
// ------- Purpose: used by BreathingRingsAnimation -------
@Composable
internal fun BreathingAnimatedRing(
    ringColor: Color,
    targetSize: Dp,
    visible: Boolean
) {
    val animatedSize by animateDpAsState(
        targetValue = if (visible) targetSize else 0.dp,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "ring_anim"
    )

    val glow by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_value"
    )

    Box(
        modifier = Modifier
            .size(animatedSize)
            .background(
                ringColor.copy(alpha = ringColor.alpha * glow),
                CircleShape
            )
    )
}
