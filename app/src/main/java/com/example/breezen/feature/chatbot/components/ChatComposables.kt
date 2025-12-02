package com.example.breezen.feature.chatbot.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.R
import com.example.breezen.core.ui.components.BackButton
import com.example.breezen.core.ui.theme.DMSansFontFamily
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.Prata

// -----------------------------------------------------
// PASTEL GREEN THEME COLORS
// -----------------------------------------------------

// -----------------------------
// DARK GREEN PREMIUM PALETTE
// -----------------------------

// Deep forest background
val APP_BACKGROUND = Color(0xFF0E1A16)           // very dark green-black

// Primary accent (emerald green)
private val APP_PRIMARY = Color(0xFF2E7D32)      // emerald / dark-green accent

// Slightly brighter variant
private val APP_PRIMARY_DARK = Color(0xFF43A047) // mid-emerald

// Text on primary surfaces
private val APP_ON_PRIMARY = Color.White

// Soft warm white for all text on dark backgrounds
private val APP_ON_SURFACE = Color(0xFFF1F8E9)   // mint-tinted off-white

// Dark muted card surface
private val APP_CARD = Color(0xFF1B2A22)         // dark desaturated green

// Icon tint (cool mint)
private val APP_ICON_TINT = Color(0xFFA5D6A7)    // mint pastel on dark

// AI bubble translucent surface
private val APP_SURFACE_TRANSLUCENT = Color(0xFF1B2A22).copy(alpha = 0.55f)


// -----------------------------------------------------
// TOP HEADER
// -----------------------------------------------------

@Composable
internal fun TopHeader(
    dailyCount: Int,
    onNewChatClicked: () -> Unit,
    navController: NavController
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(navController)

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = APP_CARD.copy(alpha = 0.7f),
                shadowElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Zeni",
                        color = APP_ON_SURFACE,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FunnelDisplayFamily,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.width(8.dp))

                    Icon(
                        painterResource(id = R.drawable.sparkles),
                        contentDescription = null,
                        tint = APP_ICON_TINT,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    val remaining = (5 - dailyCount).coerceAtLeast(0)
                    Text(
                        text = "$remaining left",
                        color = APP_ON_SURFACE.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FunnelDisplayFamily
                    )
                }
            }
        }

        ActionButton(
            icon = Icons.Default.Add,
            contentDescription = "New Chat",
            onClick = onNewChatClicked
        )
    }
}


// -----------------------------------------------------
// EMPTY STATE
// -----------------------------------------------------

@Composable
internal fun EmptyStateLarge(onSendPrompt: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        Text(
            "Hey there,",
            style = MaterialTheme.typography.titleMedium,
            color = APP_ON_SURFACE.copy(alpha = 0.75f),
            fontFamily = Prata
        )

        Spacer(Modifier.height(40.dp))

        Text(
            "How are you feeling\nthis morning?",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            color = APP_ON_SURFACE,
            textAlign = TextAlign.Center,
            fontFamily = FunnelDisplayFamily
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SuggestionButton(
                title = "Morning Calm",
                subtitle = "5-min breathing",
                icon = Icons.Outlined.SelfImprovement,
                modifier = Modifier.weight(1f),
                onClick = { onSendPrompt("Guide me through a short 5-minute morning breathing exercise.") }
            )
            SuggestionButton(
                title = "Sleep Well",
                subtitle = "Relaxation story",
                icon = Icons.Outlined.Bedtime,
                modifier = Modifier.weight(1f),
                onClick = { onSendPrompt("Tell me a calming sleep story to help me relax.") }
            )
        }
    }
}


// -----------------------------------------------------
// SUGGESTION BUTTON
// -----------------------------------------------------

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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = APP_CARD),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                null,
                tint = APP_ICON_TINT,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = APP_ON_SURFACE,
                textAlign = TextAlign.Center,
                fontFamily = Prata
            )

            Spacer(Modifier.height(4.dp))

            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = APP_ON_SURFACE.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontFamily = FunnelDisplayFamily
            )
        }
    }
}


// -----------------------------------------------------
// MESSAGE BUBBLES
// -----------------------------------------------------

@Composable
internal fun MessageBubble(
    message: String,
    sender: String,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onRegenerate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        if (sender == "AI") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    null,
                    tint = APP_PRIMARY_DARK,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Zeni",
                    color = APP_PRIMARY_DARK,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FunnelDisplayFamily
                )
            }

            Spacer(Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = APP_SURFACE_TRANSLUCENT,
                shadowElevation = 0.dp,
                modifier = Modifier.widthIn(max = 520.dp)
            ) {
                Box(modifier = Modifier.padding(14.dp)) {
                    Text(
                        message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = APP_ON_SURFACE,
                        fontFamily = DMSansFontFamily
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            ActionButtonsRow(onCopy = { onCopy(message) }, onShare = { onShare(message) }, onRegenerate)

        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = APP_PRIMARY,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Box(modifier = Modifier.padding(14.dp)) {
                        Text(
                            message,
                            color = APP_ON_PRIMARY,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = DMSansFontFamily
                        )
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------
// ACTION BUTTONS
// -----------------------------------------------------

@Composable
private fun ActionButtonsRow(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRegenerate: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionButton(Icons.Default.ThumbUp, "Like") {}
        ActionButton(Icons.Default.ThumbDown, "Dislike") {}
        ActionButton(Icons.Default.ContentCopy, "Copy", onCopy)
        ActionButton(Icons.Default.Share, "Share", onShare)
        ActionButton(Icons.Default.Refresh, "Regenerate", onRegenerate)
    }
}

@Composable
internal fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, spring())

    Surface(
        modifier = Modifier.scale(scale),
        shape = CircleShape,
        color = APP_CARD,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.size(36.dp)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription, tint = APP_ICON_TINT, modifier = Modifier.size(16.dp))
        }
    }
}


// -----------------------------------------------------
// LOADING BUBBLE
// -----------------------------------------------------

@Composable
internal fun LoadingBubble() {
    val infinite = rememberInfiniteTransition()
    val rotation by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(2000))
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.AutoAwesome, null,
            tint = APP_PRIMARY_DARK,
            modifier = Modifier.size(18.dp).rotate(rotation)
        )

        Spacer(Modifier.width(12.dp))

        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = APP_PRIMARY_DARK,
            strokeWidth = 2.dp
        )

        Spacer(Modifier.width(12.dp))

        Text(
            "Thinking...",
            color = APP_ON_SURFACE,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = Prata
        )
    }
}
