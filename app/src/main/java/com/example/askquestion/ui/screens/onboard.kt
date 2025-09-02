package com.example.askquestion.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.askquestion.R
import com.example.askquestion.theme.CustomTypography

@Composable
fun SplashScreen(
    navController: NavController,
    onOnboardingComplete: () -> Unit
) {
    val screenItems = listOf(
        listOf(1, R.drawable.splash_1, "Enter a\nCalm Space"),
        listOf(2, R.drawable.splash_2, "Controlled Breath,\nClear Mind"),
        listOf(3, R.drawable.splash_3, "Achieve\nDaily Balance"),
        listOf(4, R.drawable.splash_4, "Enhance\nSleep Quality")
    )

    var currentScreen by remember { mutableStateOf(0) }

    val progress = when (currentScreen) {
        0 -> 0f
        1 -> 1f / 3f
        2 -> 2f / 3f
        3 -> 1f
        else -> 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeContent),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            if (currentScreen > 0) {
                Button(
                    onClick = { currentScreen-- },
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.left_icon),
                        contentDescription = "Back",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        // Main content area
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = screenItems[currentScreen],
                label = "screen_transition"
            ) { screenItem ->
                OnboardingScreen(screenItem)
            }
        }

        // Bottom progress and next button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress text above the button
                Text(
                    text = "${currentScreen + 1} / 4",
                    style = CustomTypography.headlineMedium
                )

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .size(150.dp)
                            .rotate(-180f),
                        color = Color.Black,
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round,
                    )

                    Button(
                        onClick = {
                            if (currentScreen < 3) {
                                currentScreen++
                            } else {
                                // Complete onboarding and navigate to home
                                onOnboardingComplete()
                                navController.navigate("home") {
                                    popUpTo("onboard") { inclusive = true }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.Black, shape = RoundedCornerShape(100.dp)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (currentScreen < 3) "Next" else "Start",
                            style = CustomTypography.displayMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(screenItems: List<Any>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Image(
            painter = painterResource(screenItems[1] as Int),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = screenItems[2].toString().uppercase(),
            style = CustomTypography.displayLarge,
            textAlign = TextAlign.Center
        )
    }
}