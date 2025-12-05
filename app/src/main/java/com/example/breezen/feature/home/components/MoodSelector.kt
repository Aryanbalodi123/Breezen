package com.example.breezen.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MoodSelector() {
    var isComponentVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.send_mood))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        speed = 1.0f
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // When animation finishes save mood + hide UI
    LaunchedEffect(progress) {
        if (isPlaying && progress == 1f) {
            scope.launch { MoodPreference.saveMoodState(context, true) }
            isComponentVisible = false
        }
    }

    AnimatedVisibility(
        visible = isComponentVisible,
        exit = shrinkVertically(
            animationSpec = tween(500, easing = LinearEasing)
        ) + fadeOut(tween(400))
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

            val infiniteTransition = rememberInfiniteTransition()
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    tween(15000, easing = LinearEasing),
                    RepeatMode.Restart
                )
            )

            Text(
                text = "Tune Your Vibe",
                style = AppTypography.headlineLarge.copy(
                    fontFamily = FunnelDisplayFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = selectedMood.subtitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary.copy(alpha = 0.8f),
                modifier = Modifier.animateContentSize()
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(cloveShape)
            ) {
                Crossfade(
                    targetState = selectedMood,
                    animationSpec = tween(400)
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
                        .size(240.dp)
                        .clip(cloveShape)
                        .border(
                            width = 3.dp,
                            color = TextPrimary.copy(alpha = 0.4f),
                            shape = cloveShape
                        )
                        .graphicsLayer(rotationZ = rotation)
                )
            }

            Spacer(Modifier.height(48.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = selectedMoodIndex.toFloat(),
                    onValueChange = { selectedMoodIndex = it.toInt() },
                    valueRange = 0f..2f,
                    steps = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = BrandGreenDarker,
                        activeTrackColor = BrandGreenDarker.copy(alpha = 0.3f),
                        inactiveTrackColor = BrandGreenDarker.copy(alpha = 0.15f)
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(BrandGreenDarker)
                                .shadow(4.dp)
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

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        moods[0].label,
                        color = if (selectedMoodIndex == 0) TextPrimary else TextPrimary.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = if (selectedMoodIndex == 0) FontWeight.SemiBold else FontWeight.Normal
                    )

                    Text(
                        moods[2].label,
                        color = if (selectedMoodIndex == 2) TextPrimary else TextPrimary.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = if (selectedMoodIndex == 2) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .height(80.dp)
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
}

data class MoodData(
    val mood: Mood,
    val drawableId: Int,
    val label: String,
    val subtitle: String
)

enum class Mood { Happy, Sad, Angry }
