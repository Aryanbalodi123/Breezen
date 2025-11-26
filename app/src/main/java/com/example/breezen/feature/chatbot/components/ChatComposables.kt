package com.example.breezen.feature.chatbot.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.R
import com.example.breezen.core.ui.components.BackButton
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.Prata
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

// ... TopHeader remains unchanged ...
@Composable
internal fun TopHeader(onNewChatClicked: () -> Unit, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left button
        BackButton(navController)

        // Center content
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.primary
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(100f, 100f)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    "Zeni",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FunnelDisplayFamily
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    painterResource(R.drawable.sparkles),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }

        // Right button
        ActionButton(
            icon = Icons.Default.Add,
            contentDescription = "New Chat",
            onClick = onNewChatClicked
        )
    }
}

@Composable
internal fun EmptyStateLarge(
    onSendPrompt: (String) -> Unit, // Callback to send text immediately
    hazeState: HazeState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            "Hey Emma,",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontFamily = Prata
        )

        Spacer(Modifier.height(180.dp))
        Text(
            "How are you feeling\nthis morning?",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontFamily = FunnelDisplayFamily
        )
        Spacer(Modifier.height(40.dp))

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SuggestionButton(
                title = "Morning Calm",
                subtitle = "5-min breathing",
                icon = Icons.Outlined.SelfImprovement,
                modifier = Modifier.weight(1f),
                onClick = { onSendPrompt("Guide me through a short 5-minute morning breathing exercise.") },
                hazeState = hazeState
            )
            SuggestionButton(
                title = "Sleep Well",
                subtitle = "Relaxation story",
                icon = Icons.Outlined.Bedtime,
                modifier = Modifier.weight(1f),
                onClick = { onSendPrompt("Tell me a calming sleep story to help me relax.") },
                hazeState = hazeState
            )
        }
    }
}

@Composable
fun SuggestionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    hazeState: HazeState
) {
    // Glass style - Darker tint as requested
    val glassHazeStyle = HazeStyle(
        blurRadius = 24.dp,
        tint = HazeTint(Color.Black.copy(alpha = 0.55f)), // Increased opacity for darker look
        noiseFactor = 0f
    )

    Box(
        modifier = modifier
            .height(160.dp) // Adjusted height to look good with 2 lines of text
            .clip(RoundedCornerShape(32.dp))
            .hazeEffect(state = hazeState, style = glassHazeStyle)
            .background(Color.Black.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp)) // Subtle border
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                fontFamily = Prata
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontFamily = FunnelDisplayFamily
            )
        }
    }
}

// ... Rest of the file (MessageBubble, etc.) remains unchanged ...
@Composable
internal fun MessageBubble(
    message: String,
    sender: String,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onRegenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (sender == "AI") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    null,
                    tint = MaterialTheme.colorScheme.primary, // Use theme
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Zeni",
                    color = MaterialTheme.colorScheme.primary, // Use theme
                    style = MaterialTheme.typography.titleMedium, // Use theme
                    fontFamily = FunnelDisplayFamily
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Use theme glass
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = formatRichText(message),
                    style = MaterialTheme.typography.bodyMedium, // Use theme
                    color = MaterialTheme.colorScheme.onBackground, // Use theme
                    fontFamily = Prata // Apply creative font
                )
            }
            Spacer(Modifier.height(8.dp))
            ActionButtonsRow(
                onCopy = { onCopy(message) },
                onShare = { onShare(message) },
                onRegenerate = onRegenerate
            )
        } else { // User message
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondary, // Use theme
                                    MaterialTheme.colorScheme.primary // Use theme
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.onSecondary, // Use theme
                        style = MaterialTheme.typography.bodyMedium, // Use theme
                        fontFamily = Prata
                    )
                }
            }
        }
    }
}

private fun formatRichText(text: String): AnnotatedString {
    return buildAnnotatedString {
        val pattern = Regex("(?<=\\s|^)(\\*\\*|\\*)([^*]+)(\\*\\*|\\*)(?=\\s|$)")
        var lastIndex = 0
        pattern.findAll(text).forEach { matchResult ->
            val (delimiter, content) = matchResult.destructured
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last

            if (startIndex > lastIndex) {
                append(text.substring(lastIndex, startIndex))
            }
            val style = if (delimiter == "**") SpanStyle(fontWeight = FontWeight.Bold)
            else SpanStyle(fontStyle = FontStyle.Italic)
            withStyle(style) {
                append(content)
            }
            lastIndex = endIndex + 1
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
private fun ActionButtonsRow(onCopy: () -> Unit, onShare: () -> Unit, onRegenerate: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        ActionButton(icon = Icons.Default.ThumbUp, contentDescription = "Like", onClick = {})
        ActionButton(icon = Icons.Default.ThumbDown, contentDescription = "Dislike", onClick = {})
        ActionButton(
            icon = Icons.Default.ContentCopy, contentDescription = "Copy", onClick = onCopy
        )
        ActionButton(icon = Icons.Default.Share, contentDescription = "Share", onClick = onShare)
        ActionButton(
            icon = Icons.Default.Refresh, contentDescription = "Regenerate", onClick = onRegenerate
        )
    }
}

@Composable
internal fun ActionButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(), label = "")

    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)) // Use theme surface
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f),
                CircleShape
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), // Use theme
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
internal fun LoadingBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = ""
    )
    Row(
        modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            null,
            tint = MaterialTheme.colorScheme.primary, // Use theme
            modifier = Modifier
                .size(18.dp)
                .rotate(rotation)
        )
        Spacer(Modifier.width(12.dp))
        CircularProgressIndicator(
            Modifier.size(18.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Thinking...",
            color = MaterialTheme.colorScheme.onSurface, // Use theme
            style = MaterialTheme.typography.bodyMedium, // Use theme
            fontFamily = Prata
        )
    }
}