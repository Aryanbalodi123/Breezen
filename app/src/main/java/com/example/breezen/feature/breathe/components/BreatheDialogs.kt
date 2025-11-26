package com.example.breezen.feature.breathe.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.breezen.feature.breathe.model.BreathingTechnique

@Composable
internal fun TechniqueSelectorDialog(
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
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        RoundedCornerShape(28.dp)
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
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Select a breathing technique",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(techniques) { technique ->
                            EnhancedTechniqueItem(
                                technique = technique,
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
internal fun EnhancedTechniqueItem(
    technique: BreathingTechnique, isSelected: Boolean, onClick: () -> Unit, onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
            ) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(20.dp),

        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    else Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)
                        )
                    )
                )
                .border(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                    RoundedCornerShape(20.dp)
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Info button
                IconButton(
                    onClick = { onInfoClick() }, modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun TimingChip(time: String, phase: String) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$time $phase", style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold, fontSize = 10.sp
            ), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}

@Composable
internal fun TechniqueInfoDialog(
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
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(28.dp)
            ) {
                LazyColumn {
                    item {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = technique.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = technique.benefits,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary, // Use theme primary
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Close button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(),
                                    ) { onDismiss() }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Description
                        Text(
                            text = "About This Technique",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = technique.fullDescription,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Timing pattern
                        Text(
                            text = "Breathing Pattern",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Use theme colors for pattern
                        val breathingPhases = buildList {
                            add(
                                Triple(
                                    "Inhale",
                                    "${technique.inhaleTime}s",
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                            if (technique.holdTime > 0) {
                                add(
                                    Triple(
                                        "Hold",
                                        "${technique.holdTime}s",
                                        MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            add(
                                Triple(
                                    "Exhale",
                                    "${technique.exhaleTime}s",
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                            if (technique.pauseTime > 0) {
                                add(
                                    Triple(
                                        "Pause",
                                        "${technique.pauseTime}s",
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            }
                        }

                        BreathingPatternGrid(
                            phases = breathingPhases,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Instructions
                        Text(
                            text = "Step-by-Step Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
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
internal fun BreathingPatternGrid(
    phases: List<Triple<String, String, Color>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (Layout logic is fine)
        when (phases.size) {
            1 -> {
                InstructionCirclePattern(phases[0].first, phases[0].second, phases[0].third)
            }

            2 -> {
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

            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    phases.take(4).forEach { (label, time, color) ->
                        InstructionCirclePattern(label, time, color)
                    }
                }
            }
        }
    }
}

@Composable
internal fun InstructionCirclePattern(phase: String, duration: String, color: Color) {
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
                text = duration, style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ), color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = phase, style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp
            ), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
internal fun InstructionStep(stepNumber: Int, instruction: String) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape
                )
                .border(
                    1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape
                ), contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(), style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold, fontSize = 12.sp
                ), color = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        )
    }
}

@Composable
internal fun TimerSettingDialog(
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
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        RoundedCornerShape(28.dp)
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
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(
                            onClick = onDismiss, modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onBackground,
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
                        OutlinedTextField(
                            value = minutes,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                    minutes = it
                                }
                            },
                            label = {
                                Text(
                                    "Minutes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.3f
                                )
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = " : ",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 24.sp
                            ),

                            )

                        // Seconds input
                        OutlinedTextField(
                            value = seconds,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                    val sec = it.toIntOrNull() ?: 0
                                    if (sec < 60) seconds = it
                                }
                            },
                            label = {
                                Text(
                                    "Seconds",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.3f
                                )
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Quick preset buttons
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.15f
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodySmall.copy(
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
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = "Set Timer",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}