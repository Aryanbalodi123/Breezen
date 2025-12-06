package com.example.breezen.feature.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.breezen.R
import com.example.breezen.core.data.MoodPreference
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.BrandGreenDarker
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MoodSelector() {
    var isComponentVisible by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.send_mood))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        speed = 1.0f
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isComponentVisible = true
    }

    LaunchedEffect(progress) {
        if (isPlaying && progress == 1f) {
            scope.launch { MoodPreference.saveMoodState(context, true) }
            delay(200)
            isComponentVisible = false
        }
    }

    AnimatedVisibility(
        visible = isComponentVisible,
        enter = fadeIn(tween(600)) + scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
        exit = shrinkVertically(tween(500, easing = LinearEasing)) + fadeOut(tween(400))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val moods = listOf(
                MoodData(Mood.Happy, R.drawable.mood_happy, "Happy", "I'm feeling happy"),
                MoodData(Mood.Sad, R.drawable.mood_sad, "Sad", "I'm feeling sad"),
                MoodData(Mood.Angry, R.drawable.mood_angry, "Angry", "I'm feeling furious")
            )

            var selectedMoodIndex by remember { mutableStateOf(0) }
            val selectedMood = moods[selectedMoodIndex]
            val cloveShape = MaterialShapes.Clover8Leaf.toShape()

            val infiniteTransition = rememberInfiniteTransition(label = "spin")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    tween(20000, easing = LinearEasing),
                    RepeatMode.Restart
                ), label = "rotation"
            )

            Text(
                text = "Tune Your Vibe",
                style = AppTypography.headlineMedium.copy(
                    fontFamily = FunnelDisplayFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )

            Spacer(Modifier.height(24.dp))

            // Reduced Size Image
            Box(
                modifier = Modifier
                    .size(150.dp) // Reduced from 240dp
                    .clip(cloveShape)
            ) {
                Crossfade(
                    targetState = selectedMood,
                    animationSpec = tween(400),
                    label = "MoodImage"
                ) { mood ->
                    Image(
                        painter = painterResource(id = mood.drawableId),
                        contentDescription = mood.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(cloveShape)
                        .border(
                            width = 2.dp,
                            color = TextPrimary.copy(alpha = 0.3f),
                            shape = cloveShape
                        )
                        .graphicsLayer(rotationZ = rotation)
                )
            }

            Spacer(Modifier.height(24.dp))

            Slider(
                value = selectedMoodIndex.toFloat(),
                onValueChange = { selectedMoodIndex = it.toInt() },
                valueRange = 0f..2f,
                steps = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp) // Compact slider height
                    .padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = BrandGreenDarker,
                    activeTrackColor = BrandGreenDarker.copy(alpha = 0.3f),
                    inactiveTrackColor = BrandGreenDarker.copy(alpha = 0.15f)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(BrandGreenDarker)
                            .shadow(2.dp)
                    )
                },
                track = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(BrandGreenDarker.copy(alpha = 0.2f))
                    )
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    moods[0].label,
                    style = AppTypography.labelSmall,
                    color = TextPrimary.copy(alpha = if (selectedMoodIndex == 0) 1f else 0.4f)
                )
                Text(
                    moods[2].label,
                    style = AppTypography.labelSmall,
                    color = TextPrimary.copy(alpha = if (selectedMoodIndex == 2) 1f else 0.4f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Animated Text Above Button
            AnimatedContent(
                targetState = selectedMood.subtitle,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                }, label = "MoodText"
            ) { text ->
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FunnelDisplayFamily,
                    color = TextPrimary.copy(alpha = 0.9f)
                )
            }

            //  Lottie Button
            Box(
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isPlaying) isPlaying = true
                    },
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

data class MoodData(
    val mood: Mood,
    val drawableId: Int,
    val label: String,
    val subtitle: String
)

enum class Mood { Happy, Sad, Angry }