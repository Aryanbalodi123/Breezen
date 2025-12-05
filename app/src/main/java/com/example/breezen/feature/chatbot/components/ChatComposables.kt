package com.example.breezen.feature.chatbot.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.R
import com.example.breezen.core.ui.components.BackButton
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.CornerCircle
import com.example.breezen.core.ui.theme.CornerLarge
import com.example.breezen.core.ui.theme.CornerMedium
import com.example.breezen.core.ui.theme.DarkGreen
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.LightGreen
import com.example.breezen.core.ui.theme.Prata
import com.example.breezen.core.ui.theme.WhiteAlpha06
import com.example.breezen.core.ui.theme.WhiteAlpha12
import com.example.breezen.core.ui.theme.WhiteAlpha20
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

// ----------------------------------------------------------------------------------
// TOP HEADER
// ----------------------------------------------------------------------------------
@Composable
internal fun TopHeader(
    dailyCount: Int,
    onNewChatClicked: () -> Unit,
    navController: NavController
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(navController)

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(CornerCircle),
                elevation = CardDefaults.cardElevation(2.dp)
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
            onClick =  onNewChatClicked ,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WhiteAlpha12)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Back",
                tint = Color.White
            )

        }    }
}

// ----------------------------------------------------------------------------------
// EMPTY STATE
// ----------------------------------------------------------------------------------
@Composable
internal fun EmptyStateLarge(onSendPrompt: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        Text(
            text = "Hey there,",
            style = AppTypography.titleMedium,
            color = DarkGreen.copy(alpha = 0.7f),
            fontFamily = Prata
        )

        Spacer(Modifier.height(40.dp))

        Text(
            text = "How are you feeling\nthis morning?",
            style = AppTypography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            color = DarkGreen,
            textAlign = TextAlign.Center,
            fontFamily = FunnelDisplayFamily
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SuggestionButton(
                "Morning Calm",
                "5-min breathing",
                Icons.Outlined.SelfImprovement,
                Modifier.weight(1f)
            ) { onSendPrompt("Guide me through a short 5-minute morning breathing exercise.") }

            SuggestionButton(
                "Sleep Well",
                "Relaxation story",
                Icons.Outlined.Bedtime,
                Modifier.weight(1f)
            ) { onSendPrompt("Tell me a calming sleep story to help me relax.") }
        }
    }
}

// ----------------------------------------------------------------------------------
// SUGGESTION BUTTON
// ----------------------------------------------------------------------------------
@Composable
fun SuggestionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(CornerLarge),
        colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = DarkGreen, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))

            Text(
                text = title,
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DarkGreen,
                textAlign = TextAlign.Center,
                fontFamily = Prata
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = AppTypography.bodySmall,
                color = DarkGreen.copy(alpha = 0.7f),
                fontFamily = FunnelDisplayFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ----------------------------------------------------------------------------------
// MESSAGE BUBBLE
// ----------------------------------------------------------------------------------
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
    var showActions by remember(message) { mutableStateOf(isLastMessage) }
    var showContextMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {

        if (sender == "AI") {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = DarkGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Zeni",
                    color = DarkGreen,
                    style = AppTypography.titleMedium,
                    fontFamily = FunnelDisplayFamily
                )
            }

            Spacer(Modifier.height(6.dp))

            BoxWithConstraints {
                Card(
                    modifier = Modifier
                        .widthIn(max = maxWidth * 0.85f)
                        .haze(state = hazeState)
                        .combinedClickable(
                            onClick = { showActions = !showActions },
                            onLongClick = { showContextMenu = true }
                        ),
                    shape = RoundedCornerShape(CornerMedium),
                    colors = CardDefaults.cardColors(containerColor = WhiteAlpha12),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(Modifier.padding(14.dp)) {
                        Text(
                            text = message,
                            color = Color.Black,
                            style = AppTypography.bodyMedium
                        )
                    }

                    // --------------------------------------------------
                    // LONG PRESS MENU
                    // --------------------------------------------------
                    DropdownMenu(
                        expanded = showContextMenu,
                        onDismissRequest = { showContextMenu = false },
                        offset = DpOffset(0.dp, (-10).dp),
                        // FIX: Apply Shape and Color directly here
                        shape = RoundedCornerShape(50.dp),
                        containerColor = DarkGreen,
                        tonalElevation = 0.dp // Removes the grey overlay
                    ) {
                        // Just a Box for padding, no extra Card
                        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            ActionButtonsRow(
                                onCopy = { onCopy(message); showContextMenu = false },
                                onShare = { onShare(message); showContextMenu = false },
                                onRegenerate = { onRegenerate(); showContextMenu = false },
                                containerColor = WhiteAlpha20,
                                iconColor = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = showActions,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ActionButtonsRow(
                    onCopy = { onCopy(message) },
                    onShare = { onShare(message) },
                    onRegenerate = onRegenerate
                )
            }

        } else {
            // USER MESSAGE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                BoxWithConstraints {
                    Card(
                        modifier = Modifier
                            .widthIn(max = maxWidth * 0.80f)
                            .haze(state = hazeState)
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { showContextMenu = true }
                            ),
                        shape = RoundedCornerShape(CornerMedium),
                        colors = CardDefaults.cardColors(containerColor = WhiteAlpha20)
                    ) {
                        Box(Modifier.padding(14.dp)) {
                            Text(
                                text = message,
                                color = Color.Black,
                                style = AppTypography.bodyMedium
                            )
                        }


                    }
                }
            }
        }
    }
}
// --------------------------------------------------------
// ACTION BUTTONS
// --------------------------------------------------------
@Composable
private fun ActionButtonsRow(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRegenerate: () -> Unit,
    containerColor: Color = WhiteAlpha06,
    iconColor: Color = DarkGreen
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionButton(Icons.Default.ContentCopy, "Copy", containerColor, iconColor, onCopy)
        ActionButton(Icons.Default.Share, "Share", containerColor, iconColor, onShare)
        ActionButton(Icons.Default.Refresh, "Regenerate", containerColor, iconColor, onRegenerate)
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(CornerCircle),
        color = containerColor,
        tonalElevation = 2.dp,
        modifier = Modifier.size(36.dp),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ----------------------------------------------------------------------------------
// LOADING BUBBLE
// ----------------------------------------------------------------------------------
@Composable
internal fun LoadingBubble() {
    val infinite = rememberInfiniteTransition()
    val rotation by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(2000)))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AutoAwesome, null, tint = DarkGreen,
            modifier = Modifier.size(18.dp).rotate(rotation))

        Spacer(Modifier.width(12.dp))

        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = DarkGreen,
            strokeWidth = 2.dp
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = "Thinking...",
            color = DarkGreen.copy(alpha = 0.7f),
            style = AppTypography.bodyMedium,
            fontFamily = Prata
        )
    }
}