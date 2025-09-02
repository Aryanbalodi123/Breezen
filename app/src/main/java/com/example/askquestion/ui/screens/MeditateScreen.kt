package com.example.askquestion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.askquestion.R
import com.example.askquestion.theme.AppColors
import com.example.askquestion.theme.CustomTypography
import kotlinx.coroutines.delay

// Define soft pastel color palette
object MeditationColors {
    val SoftMint = Color(0xFFB8E6B8)           // Soft mint green
    val PastelLavender = Color(0xFFE6E6FA)     // Pastel lavender
    val LightPeach = Color(0xFFFFDAB9)         // Light peach
    val SoftBlue = Color(0xFFB0E0E6)           // Soft powder blue
    val PastelPink = Color(0xFFFFB6C1)         // Light pink
    val SoftYellow = Color(0xFFFFFACD)         // Lemon chiffon
    val LightLilac = Color(0xFFDDA0DD)         // Light lilac
    val PastelGreen = Color(0xFFCFF6CF)        // Pale green
    val SoftRose = Color(0xFFFFE4E1)           // Misty rose
    val LightAqua = Color(0xFFE0FFFF)
    val PastelGreen2 = Color(0xFFA7E9AF)
    // Light cyan
}
@Composable
fun MeditateScreen(navController: NavController) {
    val backgroundBrush = AppBackground()

    Box(modifier = Modifier.fillMaxSize().background(brush = backgroundBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // ✅ These will now appear at the top
            Text(
                text = "Meditation",
                style = CustomTypography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(top = 20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "What do you want to do?",
                style = CustomTypography.bodyLarge,
                color = Color.White.copy(alpha = .5f)
            )

            Spacer(modifier = Modifier.height(48.dp)) // spacing before boxes

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                MeditationCategory(
                    title = "Mind Guide",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MeditationColors.PastelGreen,
                    iconRes = R.drawable.guided_meditation
                )
                MeditationCategory(
                    title = "Relieve Stress",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MeditationColors.PastelGreen,
                    iconRes = R.drawable.relieve_stress
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                MeditationCategory(
                    title = "Relax Yourself",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MeditationColors.PastelGreen,
                    iconRes = R.drawable.relax_yourself
                )
                MeditationCategory(
                    title = "Journal",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MeditationColors.PastelGreen,
                    iconRes = R.drawable.journal
                )
            }
        }
    }
}


@Composable
fun MeditationCategory(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MeditationColors.SoftMint,
    iconRes: Int? = null,
) {
    Box(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) { /* Handle click */ }
    ) {
        // Text at the top
        Text(
            text = title,
            color = Color.Black,
            style = CustomTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        // Icon at bottom right
        iconRes?.let { icon ->
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier
                    .size(128.dp)
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.7f)),
                contentScale = ContentScale.Fit
            )
        }
    }
}


// Enhanced animated version with pastel colors
@Composable
fun AnimatedMeditationIcon(
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    val pastelShades = listOf(
        MeditationColors.SoftMint,
        MeditationColors.PastelLavender,
        MeditationColors.LightPeach,
        MeditationColors.SoftBlue,
        MeditationColors.PastelPink,
        MeditationColors.SoftYellow,
        MeditationColors.LightLilac,
        MeditationColors.PastelGreen,
        MeditationColors.SoftRose,
        MeditationColors.LightAqua
    )

    var currentColorIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // Change color every 2 seconds
            currentColorIndex = (currentColorIndex + 1) % pastelShades.size
        }
    }

    Box(
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(pastelShades[currentColorIndex]),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.7f)),
            contentScale = ContentScale.Fit
        )
    }
}
