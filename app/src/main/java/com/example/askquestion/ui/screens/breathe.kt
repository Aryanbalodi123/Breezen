package com.example.askquestion.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.askquestion.theme.CustomTypography
import kotlinx.coroutines.delay


data class BreathingTechnique(
    val id: Int,
    val name: String,
    val emoji: String,
    val shortDescription: String,
    val fullDescription: String,
    val inhaleTime: Int,
    val holdTime: Int,
    val exhaleTime: Int,
    val pauseTime: Int,
    val instructions: List<String>,
    val benefits: String
)
@Composable
fun BreatheScreen(navController: NavController) {

    val darkGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D1421), Color(0xFF1A2332), Color(0xFF243447), Color(0xFF2D4A5E)
        )
    )

    // Ring colors for dark theme
    val rings = listOf(
        RingSpec(60.dp, Color(0xFF64B5F6).copy(alpha = 0.8f)),
        RingSpec(120.dp, Color(0xFF42A5F5).copy(alpha = 0.7f)),
        RingSpec(180.dp, Color(0xFF2196F3).copy(alpha = 0.6f)),
        RingSpec(240.dp, Color(0xFF1976D2).copy(alpha = 0.5f))
    )

    val breathingTechniques = listOf(
        BreathingTechnique(
            1,
            "4-7-8 Breathing",
            "🧘‍♂️",
            "Ancient yogic technique for deep relaxation",
            "The 4-7-8 breathing technique, also known as 'relaxing breath', is based on ancient yogic practices. It helps activate the parasympathetic nervous system, reducing anxiety and promoting better sleep. This technique is particularly effective when practiced regularly before bedtime.",
            4,
            7,
            8,
            0,
            listOf(
                "Sit with your back straight",
                "Place tip of tongue against roof of mouth",
                "Exhale completely through mouth making 'whoosh' sound",
                "Close mouth and inhale through nose for 4 counts",
                "Hold breath for 7 counts",
                "Exhale through mouth for 8 counts with 'whoosh' sound",
                "Repeat cycle 3-4 times"
            ),
            "Reduces anxiety, improves sleep, lowers stress"
        ), BreathingTechnique(
            2,
            "Box Breathing",
            "🔲",
            "Equal-count breathing for focus and calm",
            "Box breathing, also called square breathing, is used by Navy SEALs and athletes for stress management. The equal timing creates a calming rhythm that helps regulate the nervous system and improve concentration. It's excellent for managing stress in high-pressure situations.",
            4,
            4,
            4,
            4,
            listOf(
                "Sit comfortably with feet flat on floor",
                "Inhale through nose for 4 counts",
                "Hold breath for 4 counts",
                "Exhale through mouth for 4 counts",
                "Hold empty lungs for 4 counts",
                "Repeat for 5-10 cycles"
            ),
            "Enhances focus, reduces stress, improves performance"
        ), BreathingTechnique(
            3,
            "Alternate Nostril",
            "🌬️",
            "Balance energy through nostril breathing",
            "Nadi Shodhana or alternate nostril breathing is a traditional pranayama technique that balances the left and right hemispheres of the brain. It helps calm the mind, reduce anxiety, and improve focus by regulating the flow of prana (life energy) through the body.",
            4,
            4,
            4,
            4,
            listOf(
                "Use right thumb to close right nostril",
                "Inhale through left nostril for 4 counts",
                "Close left nostril with ring finger",
                "Release thumb and exhale through right nostril",
                "Inhale through right nostril",
                "Close right nostril, open left and exhale",
                "Continue alternating for 5-10 rounds"
            ),
            "Balances nervous system, improves concentration"
        ), BreathingTechnique(
            4,
            "Bumblebee Breathing",
            "🐝",
            "Humming breath for stress relief",
            "Bhramari pranayama or bumblebee breathing creates vibrations that calm the mind and nervous system. The humming sound during exhalation activates the vagus nerve, promoting relaxation and reducing stress hormones. It's particularly effective for anxiety and insomnia.",
            4,
            4,
            4,
            4,
            listOf(
                "Sit with spine straight and eyes closed",
                "Place thumbs in ears to block external sounds",
                "Place index fingers above eyebrows",
                "Place remaining fingers on closed eyelids",
                "Inhale deeply through nose",
                "Exhale making a humming 'mmm' sound",
                "Focus on the vibrations in your head"
            ),
            "Reduces stress, calms mind, improves sleep"
        ), BreathingTechnique(
            5,
            "Double Inhale",
            "🌬️",
            "Quick anxiety relief technique",
            "The physiological sigh or double inhale is a natural stress-relief mechanism discovered in neuroscience research. It quickly downregulates the nervous system by optimizing the oxygen-carbon dioxide exchange, making it perfect for immediate anxiety relief.",
            2,
            0,
            5,
            0,
            listOf(
                "Take a normal inhale through nose",
                "Before exhaling, take a second smaller inhale",
                "This should fill your lungs completely",
                "Exhale slowly and completely through mouth",
                "Repeat 1-3 times as needed",
                "Use whenever feeling stressed or anxious"
            ),
            "Immediate anxiety relief, calms nervous system"
        ), BreathingTechnique(
            6,
            "Coherent Breathing",
            "🧘‍♀️",
            "Heart-rhythm synchronization",
            "Coherent breathing at 5 breaths per minute creates heart rate variability coherence, synchronizing your heart rhythm with your breathing. This technique is scientifically proven to reduce stress hormones, improve emotional regulation, and enhance overall well-being.",
            5,
            0,
            5,
            0,
            listOf(
                "Breathe at exactly 5 breaths per minute",
                "Inhale for 5 seconds through nose",
                "Exhale for 5 seconds through mouth or nose",
                "Focus on smooth, even breathing",
                "Continue for 10-20 minutes",
                "Practice daily for best results"
            ),
            "Improves heart rate variability, reduces cortisol"
        )
    )

    var selectedTechnique by remember { mutableStateOf(breathingTechniques[0]) }
    var showTechniqueSelector by remember { mutableStateOf(false) }
    var showTechniqueInfo by remember { mutableStateOf<BreathingTechnique?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var totalSessionTime by remember { mutableStateOf(300) } // 5 minutes default
    var remainingTime by remember { mutableStateOf(totalSessionTime) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // Timer effect - countdown
    LaunchedEffect(isPlaying) {
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


            .background(darkGradient)
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
                            Color.White.copy(alpha = 0.05f), CircleShape
                        )
                        .blur(20.dp)
                )
            }
        }

        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .background(
                    Color.White.copy(alpha = 0.1f), CircleShape
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Direction instruction - positioned above the circle


        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top =  24.dp , bottom = 100.dp , start = 24.dp , end = 24.dp)
                ,
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
            TechniqueSelectorDialog(techniques = breathingTechniques,
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
            }, onDismiss = { showTimerDialog = false })
        }
    }
}

@Composable
fun DirectionInstruction(
    technique: BreathingTechnique, isPlaying: Boolean
) {
    var currentPhase by remember { mutableStateOf("Ready") }

    LaunchedEffect(isPlaying, technique) {
        if (!isPlaying) {
            currentPhase = "Ready"
            return@LaunchedEffect
        }

        while (isPlaying) {
            currentPhase = "Inhale"
            delay(technique.inhaleTime * 1000L)

            if (technique.holdTime > 0) {
                currentPhase = "Hold"
                delay(technique.holdTime * 1000L)
            }

            currentPhase = "Exhale"
            delay(technique.exhaleTime * 1000L)

            if (technique.pauseTime > 0) {
                currentPhase = "Pause"
                delay(technique.pauseTime * 1000L)
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
                                Color(0xFF4ECDC4).copy(alpha = 0.2f),
                                Color(0xFF44A08D).copy(alpha = 0.2f)
                            )
                        ), RoundedCornerShape(24.dp)
                    )
                    .border(
                        1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = directionText,
                    style = CustomTypography.titleSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BreathingAnimation(
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

        while (isPlaying) {
            // Inhale phase
            currentPhase = "Inhale"
            for (i in rings.indices) {
                if (!isPlaying) break
                currentRingIndex = i
                delay(technique.inhaleTime * 1000L / rings.size)
            }

            // Hold phase
            if (technique.holdTime > 0) {
                if (!isPlaying) break
                currentPhase = "Hold"
                delay(technique.holdTime * 1000L)
            }

            // Exhale phase
            if (!isPlaying) break
            currentPhase = "Exhale"
            for (i in rings.indices.reversed()) {
                if (!isPlaying) break
                currentRingIndex = i - 1
                delay(technique.exhaleTime * 1000L / rings.size)
            }
            currentRingIndex = -1

            // Pause phase
            if (technique.pauseTime > 0) {
                if (!isPlaying) break
                currentPhase = "Pause"
                delay(technique.pauseTime * 1000L)
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
            style = CustomTypography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun EnhancedControlCard(
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
    // Only animate seconds when they actually change
    val currentSeconds = remainingTime % 60
    val currentMinutes = remainingTime / 60

    // Use a key to track when seconds actually change to prevent unnecessary animations
    val secondsKey by remember { derivedStateOf { currentSeconds } }
    val minutesKey by remember { derivedStateOf { currentMinutes } }

    // Animate only when the actual values change
    val animatedSeconds by animateIntAsState(
        targetValue = currentSeconds,
        animationSpec = tween(durationMillis = 150, easing = EaseInOut),
        label = "seconds"
    )
    val animatedMinutes by animateIntAsState(
        targetValue = currentMinutes,
        animationSpec = tween(durationMillis = 150, easing = EaseInOut),
        label = "minutes"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            ,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )

                    ,
                    RoundedCornerShape(28.dp)
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top // Align everything to top
            ) {
                // Timer section
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) { onTimerClick() }
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
                                style = CustomTypography.displayLarge.copy(fontSize = 70.sp),
                                color = Color.White,
                                modifier = Modifier.alignByBaseline().align(Alignment.Top)
                            )
                            Text(
                                text = "s",
                                style = CustomTypography.titleMedium.copy(fontSize = 16.sp),
                                color = Color.White.copy(alpha = 0.7f),
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
                                style = CustomTypography.displayLarge.copy(fontSize = 70.sp),
                                color = Color.White,
                                modifier = Modifier.alignByBaseline().align(Alignment.Top)
                            )
                            Text(
                                text = "m",
                                style = CustomTypography.titleMedium.copy(fontSize = 16.sp),
                                color = Color.White.copy(alpha = 0.7f),
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
                    // Technique selector - aligned to top
                    Card(
                        modifier = Modifier.clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) { onTechniqueClick() },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth() // Fixed width for consistency
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Style",
                                style = CustomTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) { onTechniqueClick() },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black.copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = selectedTechnique.name,
                                    style = CustomTypography.bodySmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    // Control buttons with glass effect
                    if (!isPlaying) {
                        // Full width start button with glass effect
                        Card(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth()
                                .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) { onStartClick() },
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
                                                Color(0xFF4ECDC4).copy(alpha = 0.3f),
                                                Color(0xFF4ECDC4).copy(alpha = 0.1f)
                                            )
                                        ),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF4ECDC4).copy(alpha = 0.4f),
                                                Color(0xFF4ECDC4).copy(alpha = 0.2f)
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
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Start",
                                        style = CustomTypography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        // Two buttons when playing - full width with glass effect
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Pause button with glass effect
                            Card(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f) // Take equal width
                                    .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) { onPauseClick() },
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
                                                    Color(0xFFFF9800).copy(alpha = 0.3f),
                                                    Color(0xFFFF9800).copy(alpha = 0.1f)
                                                )
                                            ),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFFF9800).copy(alpha = 0.4f),
                                                    Color(0xFFFF9800).copy(alpha = 0.2f)
                                                )
                                            ),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Stop button with glass effect
                            Card(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f) // Take equal width
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
                                                    Color(0xFFFF6B6B).copy(alpha = 0.3f),
                                                    Color(0xFFFF6B6B).copy(alpha = 0.1f)
                                                )
                                            ),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFFF6B6B).copy(alpha = 0.4f),
                                                    Color(0xFFFF6B6B).copy(alpha = 0.2f)
                                                )
                                            ),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop",
                                        tint = Color.White,
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
fun AnimatedRing(
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

@Composable
fun TechniqueSelectorDialog(
    techniques: List<BreathingTechnique>,
    selectedTechnique: BreathingTechnique,
    onTechniqueSelected: (BreathingTechnique) -> Unit,
    onInfoClick: (BreathingTechnique) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A2332).copy(alpha = 0.95f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(28.dp)
                    )
                    .padding(28.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Choose Your Style",
                                style = CustomTypography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Select a breathing technique",
                                style = CustomTypography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(techniques) { technique ->
                            EnhancedTechniqueItem(technique = technique,
                                isSelected = technique.id == selectedTechnique.id,
                                onClick = { onTechniqueSelected(technique) },
                                onInfoClick = { onInfoClick(technique) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedTechniqueItem(
    technique: BreathingTechnique, isSelected: Boolean, onClick: () -> Unit, onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4ECDC4).copy(alpha = 0.25f)
            else Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(20.dp),

        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF4ECDC4).copy(alpha = 0.2f),
                            Color(0xFF44A08D).copy(alpha = 0.2f)
                        )
                    )
                    else Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.03f)
                        )
                    )
                )
                .border(
                    1.dp, if (isSelected) Color(0xFF4ECDC4).copy(alpha = 0.6f)
                    else Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                //main content
                Row(
                    modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically
                ) {

                    Column {
                        Text(
                            text = technique.name,
                            style = CustomTypography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                // Info button
                IconButton(
                    onClick = { onInfoClick() }, modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color.White.copy(alpha = 0.1f), CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimingChip(time: String, phase: String) {
    Box(
        modifier = Modifier
            .background(
                Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$time $phase", style = CustomTypography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold, fontSize = 10.sp
            ), color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun TechniqueInfoDialog(
    technique: BreathingTechnique, onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A2332).copy(alpha = 0.95f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(28.dp)
                    )
                    .padding(28.dp)
            ) {
                LazyColumn {
                    item {
                        // Header with improved close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f) // Take available space
                            ) {
                                Text(
                                    text = technique.name,
                                    style = CustomTypography.titleLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = technique.benefits,
                                    style = CustomTypography.bodySmall,
                                    color = Color(0xFF4ECDC4),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Improved close button with better visibility
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f), // Increased opacity
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                    .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) { onDismiss() }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp) // Increased size
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Description
                        Text(
                            text = "About This Technique",
                            style = CustomTypography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = technique.fullDescription,
                            style = CustomTypography.bodyMedium.copy(
                                lineHeight = 20.sp
                            ),
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Timing pattern
                        Text(
                            text = "Breathing Pattern",
                            style = CustomTypography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Create list of breathing phases
                        val breathingPhases = buildList {
                            add(Triple("Inhale", "${technique.inhaleTime}s", Color(0xFF4ECDC4)))
                            if (technique.holdTime > 0) {
                                add(Triple("Hold", "${technique.holdTime}s", Color(0xFF45B7D1)))
                            }
                            add(Triple("Exhale", "${technique.exhaleTime}s", Color(0xFF96CEB4)))
                            if (technique.pauseTime > 0) {
                                add(Triple("Pause", "${technique.pauseTime}s", Color(0xFFFFCE68)))
                            }
                        }

                        // Grid layout for breathing pattern circles
                        BreathingPatternGrid(
                            phases = breathingPhases,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Instructions
                        Text(
                            text = "Step-by-Step Instructions",
                            style = CustomTypography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    items(technique.instructions.size) { index ->
                        InstructionStep(
                            stepNumber = index + 1,
                            instruction = technique.instructions[index]
                        )
                        if (index < technique.instructions.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun BreathingPatternGrid(
    phases: List<Triple<String, String, Color>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (phases.size) {
            1 -> {
                // Single item centered
                InstructionCirclePattern(phases[0].first, phases[0].second, phases[0].third)
            }
            2 -> {
                // Two items in one row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    phases.forEach { (label, time, color) ->
                        InstructionCirclePattern(label, time, color)
                    }
                }
            }
            3 -> {
                // Three items in one row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    phases.forEach { (label, time, color) ->
                        InstructionCirclePattern(label, time, color)
                    }
                }
            }
            4 -> {
                // 2x2 grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InstructionCirclePattern(phases[0].first, phases[0].second, phases[0].third)
                        InstructionCirclePattern(phases[1].first, phases[1].second, phases[1].third)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InstructionCirclePattern(phases[2].first, phases[2].second, phases[2].third)
                        InstructionCirclePattern(phases[3].first, phases[3].second, phases[3].third)
                    }
                }
            }
            5 -> {
                // 3 items in first row, 2 items centered in second row
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InstructionCirclePattern(phases[0].first, phases[0].second, phases[0].third)
                        InstructionCirclePattern(phases[1].first, phases[1].second, phases[1].third)
                        InstructionCirclePattern(phases[2].first, phases[2].second, phases[2].third)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(48.dp)
                    ) {
                        InstructionCirclePattern(phases[3].first, phases[3].second, phases[3].third)
                        InstructionCirclePattern(phases[4].first, phases[4].second, phases[4].third)
                    }
                }
            }
            6 -> {
                // 3x2 grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InstructionCirclePattern(phases[0].first, phases[0].second, phases[0].third)
                        InstructionCirclePattern(phases[1].first, phases[1].second, phases[1].third)
                        InstructionCirclePattern(phases[2].first, phases[2].second, phases[2].third)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InstructionCirclePattern(phases[3].first, phases[3].second, phases[3].third)
                        InstructionCirclePattern(phases[4].first, phases[4].second, phases[4].third)
                        InstructionCirclePattern(phases[5].first, phases[5].second, phases[5].third)
                    }
                }
            }
            else -> {
                // For more than 6 items, use a flexible grid with max 3 items per row
                val rows = phases.chunked(3)
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    rows.forEach { rowPhases ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (rowPhases.size == 3) {
                                Arrangement.SpaceEvenly
                            } else {
                                Arrangement.Center
                            }
                        ) {
                            rowPhases.forEachIndexed { index, (label, time, color) ->
                                InstructionCirclePattern(label, time, color)
                                // Add spacing between items when not using SpaceEvenly
                                if (rowPhases.size < 3 && index < rowPhases.size - 1) {
                                    Spacer(modifier = Modifier.width(48.dp))
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
fun InstructionCirclePattern(phase: String, duration: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color.copy(alpha = 0.2f), CircleShape
                )
                .border(
                    2.dp, color.copy(alpha = 0.5f), CircleShape
                ), contentAlignment = Alignment.Center
        ) {
            Text(
                text = duration, style = CustomTypography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ), color = Color.White
            )
        }
        Text(
            text = phase, style = CustomTypography.bodySmall.copy(
                fontSize = 12.sp
            ), color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun InstructionStep(stepNumber: Int, instruction: String) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    Color(0xFF4ECDC4).copy(alpha = 0.3f), CircleShape
                )
                .border(
                    1.dp, Color(0xFF4ECDC4).copy(alpha = 0.5f), CircleShape
                ), contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(), style = CustomTypography.bodySmall.copy(
                    fontWeight = FontWeight.Bold, fontSize = 12.sp
                ), color = Color.White
            )
        }

        Text(
            text = instruction,
            style = CustomTypography.bodyMedium.copy(
                lineHeight = 20.sp
            ),
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        )
    }
}

@Composable
fun TimerSettingDialog(
    currentTime: Int, onTimeSet: (Int) -> Unit, onDismiss: () -> Unit
) {
    var minutes by remember { mutableStateOf((currentTime / 60).toString()) }
    var seconds by remember { mutableStateOf((currentTime % 60).toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A2332).copy(alpha = 0.95f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(28.dp)
                    )
                    .padding(28.dp)
            ) {
                Column {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Set Timer",
                            style = CustomTypography.headlineMedium,
                            color = Color.White
                        )
                        IconButton(
                            onClick = onDismiss, modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color.White.copy(alpha = 0.1f), CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Time input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Minutes input
                        OutlinedTextField(value = minutes,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                    minutes = it
                                }
                            },
                            label = {
                                Text(
                                    "Minutes",
                                    style = CustomTypography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4ECDC4),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            textStyle = CustomTypography.bodyMedium
                        )

                        Text(
                            text = " : ",
                            color = Color.White,
                            style = CustomTypography.headlineMedium.copy(
                                fontSize = 24.sp
                            ),

                        )

                        // Seconds input
                        OutlinedTextField(value = seconds,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                    val sec = it.toIntOrNull() ?: 0
                                    if (sec < 60) seconds = it
                                }
                            },
                            label = {
                                Text(
                                    "Seconds",
                                    style = CustomTypography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4ECDC4),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            textStyle = CustomTypography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Quick preset buttons
                    Text(
                        text = "Quick Presets", style = CustomTypography.titleMedium.copy(
                            fontSize = 16.sp
                        ), color = Color.White, modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            "1 min" to 60, "5 min" to 300, "10 min" to 600, "15 min" to 900
                        ).forEach { (label, time) ->

                               Button(
                                   onClick = {
                                       minutes = (time / 60).toString()
                                       seconds = (time % 60).toString()
                                   },
                                   modifier = Modifier.fillMaxWidth().height(36.dp),
                                   colors = ButtonDefaults.buttonColors(
                                       containerColor = Color.White.copy(alpha = 0.15f)
                                   ),
                                   shape = RoundedCornerShape(8.dp)
                               ) {
                                   Text(
                                       text = label,
                                       color = Color.White,
                                       style = CustomTypography.bodySmall.copy(
                                           fontSize = 12.sp
                                       )
                                   )
                               }

                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Set button
                    Button(
                        onClick = {
                            val totalSeconds =
                                (minutes.toIntOrNull() ?: 0) * 60 + (seconds.toIntOrNull() ?: 0)
                            if (totalSeconds > 0) {
                                onTimeSet(totalSeconds)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4ECDC4).copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = "Set Timer",
                            color = Color.White,
                            style = CustomTypography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

data class RingSpec(val size: Dp, val color: Color)