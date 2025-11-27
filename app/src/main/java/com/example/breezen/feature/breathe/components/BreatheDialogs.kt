package com.example.breezen.feature.breathe.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppGray
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.BrandGreen
import com.example.breezen.core.ui.theme.BrandGreenBright
import com.example.breezen.core.ui.theme.BrandGreenDarker
import com.example.breezen.core.ui.theme.TextPrimary
import com.example.breezen.core.ui.theme.TextSecondary
import com.example.breezen.feature.breathe.model.BreathingTechnique
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

// --- SHARED GLASS STYLES ---

@Composable
private fun GlassOverlayContainer(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    // This Box acts as the "Dim" background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)) // Darker dim for focus
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // No ripple on background click
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Prevent clicks from passing through the dialog card
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Do nothing */ },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun GlassCard(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val glassStyle = HazeStyle(
        blurRadius = 48.dp,
        tint = HazeTint(Color.Black.copy(alpha = 0.9f)), // Dark glass
        noiseFactor = 0.5f
    )

    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .hazeEffect(state = hazeState, style = glassStyle)
            .background(Color.White.copy(alpha = 0.03f)) // Subtle surface fill
            .border(1.dp, borderBrush, RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        content()
    }
}

// --- DIALOGS ---

@Composable
internal fun TechniqueSelectorDialog(
    hazeState: HazeState,
    techniques: List<BreathingTechnique>,
    selectedTechnique: BreathingTechnique,
    onTechniqueSelected: (BreathingTechnique) -> Unit,
    onInfoClick: (BreathingTechnique) -> Unit,
    onDismiss: () -> Unit
) {
    GlassOverlayContainer(onDismiss = onDismiss) {
        GlassCard(
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(500.dp)
        ) {
            Column {
                Text(
                    text = "Choose Your Style",
                    style = AppTypography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Select a breathing technique",
                    style = AppTypography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(techniques) { technique ->
                        GlassTechniqueItem(
                            technique = technique,
                            isSelected = technique.id == selectedTechnique.id,
                            onClick = { onTechniqueSelected(technique) },
                            onInfoClick = { onInfoClick(technique) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun GlassTechniqueItem(
    technique: BreathingTechnique,
    isSelected: Boolean,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val backgroundColor = if (isSelected) BrandGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
    val borderColor = if (isSelected) BrandGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
    val contentColor = if (isSelected) BrandGreen else TextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = BrandGreen)
            ) { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = technique.name,
                style = AppTypography.titleMedium,
                color = contentColor
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selected",
                    style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
            }
        }

        IconButton(
            onClick = { onInfoClick() },
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isSelected) BrandGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Info",
                tint = contentColor
            )
        }
    }
}

@Composable
internal fun TechniqueInfoDialog(
    hazeState: HazeState,
    technique: BreathingTechnique,
    onDismiss: () -> Unit
) {
    GlassOverlayContainer(onDismiss = onDismiss) {
        GlassCard(
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = technique.name,
                            style = AppTypography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = technique.benefits,
                            style = AppTypography.titleMedium,
                            color = BrandGreen,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text(
                            text = "About This Technique",
                            style = AppTypography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = technique.fullDescription,
                            style = AppTypography.bodyLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Text(
                            text = "Breathing Pattern",
                            style = AppTypography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        val breathingPhases = buildList {
                            add(Triple("Inhale", "${technique.inhaleTime}s", BrandGreen))
                            if (technique.holdTime > 0) add(Triple("Hold", "${technique.holdTime}s", BrandGreenBright))
                            add(Triple("Exhale", "${technique.exhaleTime}s", BrandGreenDarker))
                            if (technique.pauseTime > 0) add(Triple("Pause", "${technique.pauseTime}s", AppGray))
                        }

                        BreathingPatternGrid(phases = breathingPhases, modifier = Modifier.padding(bottom = 24.dp))

                        Text(
                            text = "Step-by-Step Instructions",
                            style = AppTypography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    items(technique.instructions.size) { index ->
                        GlassInstructionStep(
                            stepNumber = index + 1,
                            instruction = technique.instructions[index]
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun BreathingPatternGrid(
    phases: List<Triple<String, String, Color>>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        phases.forEach { (label, time, color) ->
            GlassInstructionCirclePattern(label, time, color)
        }
    }
}

@Composable
internal fun GlassInstructionCirclePattern(phase: String, duration: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(color.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = duration, style = AppTypography.headlineMedium, color = TextPrimary)
        }
        Text(
            text = phase,
            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
internal fun GlassInstructionStep(stepNumber: Int, instruction: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(BrandGreen.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, BrandGreen.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = BrandGreen
            )
        }
        Text(
            text = instruction,
            style = AppTypography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        )
    }
}

@Composable
internal fun TimerSettingDialog(
    hazeState: HazeState,
    currentTime: Int,
    onTimeSet: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var minutes by remember { mutableStateOf((currentTime / 60).toString()) }
    var seconds by remember { mutableStateOf((currentTime % 60).toString()) }

    GlassOverlayContainer(onDismiss = onDismiss) {
        GlassCard(
            hazeState = hazeState,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Set Timer", style = AppTypography.headlineMedium, color = TextPrimary)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassTextField(
                        value = minutes,
                        onValueChange = { if (it.length <= 2) minutes = it.filter { c -> c.isDigit() } },
                        label = "Min"
                    )
                    Text(" : ", style = AppTypography.headlineLarge, color = TextPrimary, modifier = Modifier.padding(horizontal = 12.dp))
                    GlassTextField(
                        value = seconds,
                        onValueChange = { if (it.length <= 2) seconds = it.filter { c -> c.isDigit() } },
                        label = "Sec"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(300 to "5m", 600 to "10m", 900 to "15m").forEach { (sec, label) ->
                        OutlinedButton(
                            onClick = { minutes = (sec / 60).toString(); seconds = (sec % 60).toString() },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandGreen.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen)
                        ) {
                            Text(label, style = AppTypography.bodyLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val total = (minutes.toIntOrNull() ?: 0) * 60 + (seconds.toIntOrNull() ?: 0)
                        if (total > 0) onTimeSet(total)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = AppBlack)
                ) {
                    Text("Start Session", style = AppTypography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = AppTypography.headlineMedium.copy(color = TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(BrandGreen)
            )
        }
        Text(text = label, style = AppTypography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
    }
}