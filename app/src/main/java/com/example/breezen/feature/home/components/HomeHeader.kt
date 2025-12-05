package com.example.breezen.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.navigation.NavController
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.BrandGreenBright
import com.example.breezen.core.ui.theme.BrandGreenDarker
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun AppHeader(
    username: String,
    navController: NavController
) {
    var breezenText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        "Breezen".forEach { char ->
            breezenText += char
            delay(150)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBlack)
            .padding(horizontal = 16.dp)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = breezenText,
            color = TextPrimary,
            style = AppTypography.titleLarge.copy(
                fontFamily = FunnelDisplayFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp,
                fontSize = 32.sp
            )
        )

        Spacer(Modifier.weight(1f))

        UserAvatar(username = username, navController = navController)
    }
}

@Composable
fun UserAvatar(
    username: String,
    navController: NavController
) {
    val initial = username.firstOrNull()?.uppercaseChar() ?: '?'

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            BrandGreenBright,
            BrandGreenDarker
        )
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(gradientBrush)
            .clickable { navController.navigate("settings") },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.toString(),
            color = AppBlack,
            style = AppTypography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        )
    }
}
