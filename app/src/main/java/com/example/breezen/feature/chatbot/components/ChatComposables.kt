package com.example.breezen.feature.chatbot.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.R
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.Prata
import com.example.breezen.core.ui.theme.SystemStop


@Composable
internal fun TopHeader(onNewChatClicked: () -> Unit, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left button
        ActionButton(
            icon = Icons.Default.ArrowBack, contentDescription = "Back",
            onClick = {

                navController.popBackStack()
            },
        )

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
                            // Use theme colors for the gradient
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
                    "ZenAI",
                    color = MaterialTheme.colorScheme.onSecondary, // Use theme
                    style = MaterialTheme.typography.titleMedium // Use theme
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    painterResource(R.drawable.sparkles),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondary // Use theme
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
internal fun EmptyStateLarge() {
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(Modifier.height(24.dp))
            Text(
                "Hi there!",
                style = MaterialTheme.typography.displayLarge, // Use theme
                color = MaterialTheme.colorScheme.onBackground, // Use theme
                fontFamily = FunnelDisplayFamily // Apply creative font
            )
            Text(
                "How can I help you?",
                style = MaterialTheme.typography.titleMedium, // Use theme
                color = MaterialTheme.colorScheme.onBackground, // Use theme
                fontFamily = Prata // Apply creative font
            )
        }
    }
}

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
                    "Zeni AI",
                    color = MaterialTheme.colorScheme.primary, // Use theme
                    style = MaterialTheme.typography.titleMedium // Use theme
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
                        style = MaterialTheme.typography.bodyMedium // Use theme
                    )
                }
            }
        }
    }
}

private fun formatRichText(text: String): AnnotatedString {
    // A simple markdown parser for bold (**) and italics (*)
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
            style = MaterialTheme.typography.bodyMedium // Use theme
        )
    }
}

@Composable
internal fun BottomInputSection(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    loading: Boolean
) {
    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f), // Use theme
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = loading
            ) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = SystemStop) // Use specific system color
                ) {
                    Icon(
                        Icons.Default.Stop,
                        "Stop",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Stop generating", color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), // Use theme
                            MaterialTheme.shapes.extraLarge
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
                    decorationBox = { innerTextField ->
                        if (input.isEmpty()) {
                            Text(
                                "Ask me anything...",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        innerTextField()
                    })
                val sendButtonEnabled = input.isNotBlank() && !loading
                IconButton(
                    onClick = onSend,
                    enabled = sendButtonEnabled,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (sendButtonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.12f
                        ),
                        contentColor = if (sendButtonEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.38f
                        )
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}