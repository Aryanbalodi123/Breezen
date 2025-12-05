package com.example.breezen.feature.breathe.components

import androidx.compose.foundation.BorderStroke
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
import com.example.breezen.core.ui.theme.BlackAlpha60
import com.example.breezen.core.ui.theme.BlackAlpha90
import com.example.breezen.core.ui.theme.BrandGreen
import com.example.breezen.core.ui.theme.BrandGreenBright
import com.example.breezen.core.ui.theme.BrandGreenDarker
import com.example.breezen.core.ui.theme.CornerCircle
import com.example.breezen.core.ui.theme.CornerLarge
import com.example.breezen.core.ui.theme.CornerMedium
import com.example.breezen.core.ui.theme.CornerXLarge
import com.example.breezen.core.ui.theme.TextPrimary
import com.example.breezen.core.ui.theme.TextSecondary
import com.example.breezen.core.ui.theme.WhiteAlpha03
import com.example.breezen.core.ui.theme.WhiteAlpha05
import com.example.breezen.core.ui.theme.WhiteAlpha08
import com.example.breezen.core.ui.theme.WhiteAlpha10
import com.example.breezen.core.ui.theme.WhiteAlpha15
import com.example.breezen.core.ui.theme.WhiteAlpha20
import com.example.breezen.feature.breathe.model.BreathingTechnique
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/* ---------------------------------------------------------
   ------- DIALOG OVERLAY CONTAINER -------
   Purpose: Dim background + handle outside dismissal
   --------------------------------------------------------- */
@Composable
private fun DialogOverlayContainer(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackAlpha60)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Prevent background clicks from passing into card
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/* ---------------------------------------------------------
   ------- DIALOG CONTAINER -------
   Purpose: Blurred card used for all breathe dialogs
   --------------------------------------------------------- */
@Composable
private fun DialogContainer(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hazeStyle = HazeStyle(
        blurRadius = 48.dp,
        tint = HazeTint(BlackAlpha90),
        noiseFactor = 0.5f
    )

    val borderBrush = Brush.verticalGradient(
        listOf(WhiteAlpha15, WhiteAlpha05)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CornerXLarge))
            .hazeEffect(hazeState, hazeStyle)
            .background(WhiteAlpha03)
            .border(1.dp, borderBrush, RoundedCornerShape(CornerXLarge))
            .padding(24.dp)
    ) {
        content()
    }
}

/* ---------------------------------------------------------
   ------- TECHNIQUE SELECTOR DIALOG -------
   Purpose: User chooses breathing technique
   --------------------------------------------------------- */
@Composable
internal fun TechniqueSelectorDialog(
    hazeState: HazeState,
    techniques: List<BreathingTechnique>,
    selectedTechnique: BreathingTechnique,
    onTechniqueSelected: (BreathingTechnique) -> Unit,
    onInfoClick: (BreathingTechnique) -> Unit,
    onDismiss: () -> Unit
) {
    DialogOverlayContainer(onDismiss) {
        DialogContainer(
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(500.dp)
        ) {
            Column {
                Text("Choose Your Style", style = AppTypography.headlineMedium, color = TextPrimary)
                Text(
                    "Select a breathing technique",
                    style = AppTypography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(techniques) { technique ->
                        TechniqueSelectorItem(
                            technique = technique,
                            isSelected = technique.id == selectedTechnique.id,
                            onSelect = { onTechniqueSelected(technique) },
                            onInfoClick = { onInfoClick(technique) }
                        )
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------
   ------- TECHNIQUE SELECTOR ITEM -------
   Purpose: Single clickable breathing technique row
   --------------------------------------------------------- */
@Composable
private fun TechniqueSelectorItem(
    technique: BreathingTechnique,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onInfoClick: () -> Unit
) {
    val bgColor = if (isSelected) BrandGreen.copy(alpha = 0.15f) else WhiteAlpha05
    val borderColor = if (isSelected) BrandGreen.copy(alpha = 0.5f) else WhiteAlpha10
    val contentColor = if (isSelected) BrandGreen else TextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerLarge))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(CornerLarge))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = BrandGreen)
            ) { onSelect() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(technique.name, style = AppTypography.titleMedium, color = contentColor)
            if (isSelected) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Selected",
                    style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
            }
        }

        IconButton(
            onClick = onInfoClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isSelected) BrandGreen.copy(alpha = 0.2f) else WhiteAlpha10,
                    CircleShape
                )
        ) {
            Icon(Icons.Default.Info, "Info", tint = contentColor)
        }
    }
}

/* ---------------------------------------------------------
   ------- TECHNIQUE INFO DIALOG -------
   Purpose: Full details of breathing technique
   --------------------------------------------------------- */
@Composable
internal fun TechniqueInfoDialog(
    hazeState: HazeState,
    technique: BreathingTechnique,
    onDismiss: () -> Unit
) {
    DialogOverlayContainer(onDismiss) {
        DialogContainer(
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(technique.name, style = AppTypography.headlineMedium, color = TextPrimary)
                        Text(
                            technique.benefits,
                            style = AppTypography.titleMedium,
                            color = BrandGreen,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(WhiteAlpha10, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = TextPrimary)
                    }
                }

                Spacer(Modifier.height(24.dp))

                LazyColumn(Modifier.weight(1f)) {
                    item {
                        Text("About This Technique", style = AppTypography.titleMedium, color = TextPrimary)
                        Text(
                            technique.fullDescription,
                            style = AppTypography.bodyLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Text("Breathing Pattern", style = AppTypography.titleMedium, color = TextPrimary)
                        val phases = buildList {
                            add(Triple("Inhale", "${technique.inhaleTime}s", BrandGreen))
                            if (technique.holdTime > 0) add(Triple("Hold", "${technique.holdTime}s", BrandGreenBright))
                            add(Triple("Exhale", "${technique.exhaleTime}s", BrandGreenDarker))
                            if (technique.pauseTime > 0) add(Triple("Pause", "${technique.pauseTime}s", AppGray))
                        }
                        BreathingPatternGrid(phases, Modifier.padding(vertical = 24.dp))

                        Text("Step-by-Step Instructions", style = AppTypography.titleMedium, color = TextPrimary)
                    }

                    items(technique.instructions.size) { index ->
                        TechniqueInstructionStep(
                            stepNumber = index + 1,
                            instruction = technique.instructions[index]
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------
   ------- BREATHING PATTERN GRID -------
   Purpose: Shows inhale / hold / exhale / pause pattern
   --------------------------------------------------------- */
@Composable
private fun BreathingPatternGrid(
    phases: List<Triple<String, String, Color>>,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        phases.forEach { (label, time, color) ->
            TechniquePatternCircle(label, time, color)
        }
    }
}

/* ---------------------------------------------------------
   ------- TECHNIQUE PATTERN CIRCLE -------
   Purpose: Circle showing duration of each phase
   --------------------------------------------------------- */
@Composable
private fun TechniquePatternCircle(
    phase: String,
    duration: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(color.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(duration, style = AppTypography.headlineMedium, color = TextPrimary)
        }
        Text(
            phase,
            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/* ---------------------------------------------------------
   ------- TECHNIQUE INSTRUCTION STEP -------
   Purpose: Numbered steps showing how to perform technique
   --------------------------------------------------------- */
@Composable
private fun TechniqueInstructionStep(
    stepNumber: Int,
    instruction: String
) {
    Row(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(BrandGreen.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, BrandGreen.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$stepNumber",
                style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = BrandGreen
            )
        }

        Text(
            instruction,
            style = AppTypography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        )
    }
}

/* ---------------------------------------------------------
   ------- TIMER SETTING DIALOG -------
   Purpose: User sets breathing session duration
   --------------------------------------------------------- */
@Composable
internal fun TimerSettingDialog(
    hazeState: HazeState,
    currentTime: Int,
    onTimeSet: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var minutes by remember { mutableStateOf((currentTime / 60).toString()) }
    var seconds by remember { mutableStateOf((currentTime % 60).toString()) }

    DialogOverlayContainer(onDismiss) {
        DialogContainer(
            hazeState = hazeState,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Set Timer", style = AppTypography.headlineMedium, color = TextPrimary)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(WhiteAlpha10, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = TextPrimary)
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimerInputField(
                        value = minutes,
                        onValueChange = { if (it.length <= 2) minutes = it.filter(Char::isDigit) },
                        label = "Min"
                    )
                    Text(" : ", style = AppTypography.headlineLarge, color = TextPrimary, modifier = Modifier.padding(horizontal = 12.dp))
                    TimerInputField(
                        value = seconds,
                        onValueChange = { if (it.length <= 2) seconds = it.filter(Char::isDigit) },
                        label = "Sec"
                    )
                }

                Spacer(Modifier.height(32.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(300 to "5m", 600 to "10m", 900 to "15m").forEach { (sec, label) ->
                        OutlinedButton(
                            onClick = {
                                minutes = (sec / 60).toString()
                                seconds = (sec % 60).toString()
                            },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(CornerCircle),
                            border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen)
                        ) {
                            Text(label, style = AppTypography.bodyLarge)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        val total = (minutes.toIntOrNull() ?: 0) * 60 +
                                (seconds.toIntOrNull() ?: 0)
                        if (total > 0) onTimeSet(total)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(CornerMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = AppBlack
                    )
                ) {
                    Text("Start Session", style = AppTypography.titleMedium)
                }
            }
        }
    }
}

/* ---------------------------------------------------------
   ------- TIMER INPUT FIELD -------
   Purpose: Minutes / seconds input for timer
   --------------------------------------------------------- */
@Composable
private fun TimerInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(CornerMedium))
                .background(WhiteAlpha08)
                .border(1.dp, WhiteAlpha20, RoundedCornerShape(CornerMedium)),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = AppTypography.headlineMedium.copy(
                    color = TextPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(BrandGreen)
            )
        }
        Text(label, style = AppTypography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
    }
}

