package com.example.askquestion.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.askquestion.core.ui.theme.FunnelDisplayFamily
import kotlinx.coroutines.delay

@Composable
fun AppHeader(username: String) {
    var breezenText by remember { mutableStateOf("") }

    // Typewriter effect animation
    LaunchedEffect(Unit) {
        "Breezen".forEach { char ->
            breezenText += char
            delay(150)
        }
    }

    Row(
        Modifier
            .fillMaxWidth()

            .padding(horizontal = 16.dp)
            .height(50.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            breezenText,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FunnelDisplayFamily, // Apply creative font
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp,
                fontSize = 32.sp
            )
        )
        Spacer(Modifier.weight(1f))

        UserAvatar(username = username)
    }
}

@Composable
fun UserAvatar(username: String) {
    val initial = username.firstOrNull()?.uppercaseChar() ?: '?'
    // This is an artistic choice, not a theme color, so it stays
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.primary
        )
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.toString(),
            color = MaterialTheme.colorScheme.onPrimary, // Use onPrimary
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}