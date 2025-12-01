package com.example.breezen.feature.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.BrandGreen
import com.example.breezen.core.ui.theme.GlassBackground
import com.example.breezen.core.ui.theme.GlassBorder
import com.example.breezen.core.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ------------------------------------------------------------
//  COLORFUL HEADER CARD
// ------------------------------------------------------------
@Composable
fun ColorfulHeaderCard(
    initial: String,
    title: String?,
    subtitle: String?,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(accent.copy(alpha = 0.18f), GlassBackground)
                    )
                )
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial, style = AppTypography.displayMedium, color = AppBlack)
                }

                Spacer(Modifier.height(12.dp))

                if (title != null) {
                    Text(title, style = AppTypography.headlineMedium, color = AppWhite)
                }

                if (!subtitle.isNullOrEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        subtitle,
                        style = AppTypography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------
//  SETTINGS TILE
// ------------------------------------------------------------
@Composable
fun SettingsTile(
    title: String,
    subtitle: String? = null,
    accent: Color = BrandGreen,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) {
                pressed = true
                scope.launch {
                    delay(120)
                    pressed = false
                }
                onClick()
            }
            .padding(vertical = 14.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Icon box
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                title.take(1).uppercase(),
                style = AppTypography.titleMedium,
                color = accent
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = AppTypography.titleMedium, color = AppWhite)
            subtitle?.let {
                Text(it, style = AppTypography.bodySmall, color = TextSecondary)
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}

// ------------------------------------------------------------
//  GLASS FIELD
// ------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        singleLine = true,
        visualTransformation =
            if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandGreen,
            unfocusedBorderColor = GlassBorder,
            focusedContainerColor = GlassBackground,
            unfocusedContainerColor = GlassBackground,
            cursorColor = BrandGreen,
            focusedTextColor = AppWhite,
            unfocusedTextColor = AppWhite,
            focusedLabelColor = BrandGreen,
            unfocusedLabelColor = TextSecondary
        )
    )
}

// ------------------------------------------------------------
//  ACTION BUTTON
// ------------------------------------------------------------
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = BrandGreen
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text = text, color = AppBlack, style = AppTypography.titleMedium)
    }
}
