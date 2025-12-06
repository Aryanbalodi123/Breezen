package com.example.breezen.feature.chatbot.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.R
import com.example.breezen.core.ui.components.BackButton
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.CornerCircle
import com.example.breezen.core.ui.theme.CornerLarge
import com.example.breezen.core.ui.theme.DarkGreen
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.LightGreen
import com.example.breezen.core.ui.theme.Prata
import com.example.breezen.core.ui.theme.WhiteAlpha06
import com.example.breezen.core.ui.theme.WhiteAlpha12
import com.example.breezen.core.ui.theme.WhiteAlpha20
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.delay

// --- Helper for iOS Style Bouncy Touch ---
@Composable
fun IOSBouncyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    shape: RoundedCornerShape,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // iOS Scale Logic: Shrink to 0.96 when held, snap back to 1.0 when released
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iosScale"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp) // iOS usually has flat cards or subtle shadow
    ) {
        content()
    }
}

@Composable
internal fun TopHeader(
    dailyCount: Int,
    onNewChatClicked: () -> Unit,
    navController: NavController
) {
    // Entrance: A subtle scale-up with no overshoot, very clean.
    val scale = remember { Animatable(0.9f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(400))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .graphicsLayer { this.alpha = alpha.value },
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(navController)

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
                colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(CornerCircle),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zeni",
                        color = LightGreen,
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FunnelDisplayFamily
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.sparkles),
                        contentDescription = null,
                        tint = LightGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    val remaining = (5 - dailyCount).coerceAtLeast(0)
                    Text(
                        text = "|  $remaining left",
                        color = LightGreen,
                        style = AppTypography.titleMedium,
                        fontFamily = FunnelDisplayFamily
                    )
                }
            }
        }

        IconButton(
            onClick = onNewChatClicked,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WhiteAlpha12)
        ) {
            Icon(Icons.Default.Add, "New Chat", tint = Color.White)
        }
    }
}

@Composable
internal fun EmptyStateLarge(onSendPrompt: (String) -> Unit) {
    // Title slides up with a "heavy" feel (Quart easing)
    var showTitle by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showTitle = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        AnimatedVisibility(
            visible = showTitle,
            enter = fadeIn(tween(800)) + slideInVertically(
                animationSpec = tween(800, easing = EaseOutQuart)
            ) { it / 2 }
        ) {
            Text(
                text = "How are you feeling\nthis morning?",
                style = AppTypography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                color = DarkGreen,
                textAlign = TextAlign.Center,
                fontFamily = FunnelDisplayFamily
            )
        }

        Spacer(Modifier.weight(1f))

        // Two boxes in a row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Tighter, precise spacing
        ) {
            SuggestionButton(
                title = "Morning Calm",
                subtitle = "5-min breathing",
                icon = Icons.Outlined.SelfImprovement,
                modifier = Modifier.weight(1f),
                delayMillis = 100 // Slight stagger
            ) { onSendPrompt("Guide me through a short 5-minute morning breathing exercise.") }

            SuggestionButton(
                title = "Sleep Well",
                subtitle = "Relaxation story",
                icon = Icons.Outlined.Bedtime,
                modifier = Modifier.weight(1f),
                delayMillis = 200
            ) { onSendPrompt("Tell me a calming sleep story to help me relax.") }
        }
    }
}

@Composable
fun SuggestionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    delayMillis: Long,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        // iOS Spring Entrance: Snappy pop-up
        enter = fadeIn(tween(400)) +
                slideInVertically(spring(dampingRatio = 0.7f, stiffness = 400f)) { it / 2 } +
                scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = 0.6f)),
        exit = fadeOut()
    ) {
        // Use the custom iOS Bouncy Card
        IOSBouncyCard(
            onClick = onClick,
            modifier = Modifier.height(130.dp),
            shape = RoundedCornerShape(CornerLarge),
            containerColor = DarkGreen.copy(alpha = 0.15f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(icon, null, tint = DarkGreen, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = title,
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkGreen,
                    fontFamily = Prata,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = AppTypography.bodySmall,
                    color = DarkGreen.copy(alpha = 0.7f),
                    fontFamily = FunnelDisplayFamily,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: String,
    sender: String,
    isLastMessage: Boolean,
    hazeState: HazeState,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onRegenerate: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // iMessage Style Entrance:
    // Bubbles don't just fade; they "pop" in from their anchor point (bottom-left or bottom-right).
    val transformOrigin = if (sender == "AI") TransformOrigin(0f, 1f) else TransformOrigin(1f, 1f)

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
            transformOrigin = transformOrigin
        ) + fadeIn(tween(300)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            if (sender == "AI") {
                // ... (Avatar code remains similar, simplified for brevity) ...
                val spin by rememberInfiniteTransition().animateFloat(
                    initialValue = 0f, targetValue = 360f,
                    animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = DarkGreen, modifier = Modifier.size(18.dp).rotate(spin))
                    Spacer(Modifier.width(8.dp))
                    Text("Zeni", color = DarkGreen, style = AppTypography.titleMedium, fontFamily = FunnelDisplayFamily)
                }
                Spacer(Modifier.height(6.dp))

                var showActions by remember(message) { mutableStateOf(isLastMessage) }

                // Using BoxWithConstraints to ensure bubble doesn't stretch too far
                BoxWithConstraints {
                    Card(
                        modifier = Modifier
                            .widthIn(max = maxWidth * 0.85f)
                            .haze(state = hazeState)
                            .combinedClickable(
                                onClick = { showActions = !showActions },
                                onLongClick = { /* Handle Context Menu */ }
                            ),
                        shape = RoundedCornerShape(
                            topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = WhiteAlpha12),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(Modifier.padding(16.dp)) {
                            Text(message, color = Color.Black, style = AppTypography.bodyMedium, fontFamily = Prata)
                        }
                    }
                }

                // Action Buttons fade in below
                AnimatedVisibility(
                    visible = showActions,
                    enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()
                ) {
                    Spacer(Modifier.height(8.dp))
                    ActionButtonsRow(onCopy = { onCopy(message) }, onShare = { onShare(message) }, onRegenerate = onRegenerate)
                }

            } else {
                // User Bubble
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Card(
                        modifier = Modifier.widthIn(max = 300.dp).haze(state = hazeState),
                        shape = RoundedCornerShape(
                            topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 20.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = WhiteAlpha20),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(Modifier.padding(16.dp)) {
                            Text(message, color = Color.Black, style = AppTypography.bodyMedium, fontFamily = Prata)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRegenerate: () -> Unit,
    containerColor: Color = WhiteAlpha06,
    iconColor: Color = DarkGreen
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionButton(Icons.Default.ContentCopy, containerColor, iconColor, onCopy)
        ActionButton(Icons.Default.Share, containerColor, iconColor, onShare)
        ActionButton(Icons.Default.Refresh, containerColor, iconColor, onRegenerate)
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    // Mini iOS Interaction
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.85f else 1f)

    Surface(
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
internal fun LoadingBubble() {
    // Simple pulse breathing animation for loading
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.graphicsLayer { this.alpha = alpha }
    ) {
        Icon(Icons.Default.AutoAwesome, null, tint = DarkGreen, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text("Thinking...", color = DarkGreen, style = AppTypography.bodyMedium, fontFamily = Prata)
    }
}