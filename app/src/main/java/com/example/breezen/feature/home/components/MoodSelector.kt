package com.example.breezen.feature.home.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breezen.R
import com.example.breezen.core.ui.theme.FunnelDisplayFamily

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MoodSelector() {
    val moods = listOf(
        MoodData(Mood.Happy, R.drawable.mood_happy, "Happy", "I'm feeling happy"),
        MoodData(Mood.Sad, R.drawable.mood_sad, "Sad", "I'm feeling sad"),
        MoodData(Mood.Angry, R.drawable.mood_angry, "Angry", "I'm feeling furious")
    )

    var selectedMoodIndex by remember { mutableStateOf(0) } // Start with Happy
    val selectedMood = moods[selectedMoodIndex]
    val CloveShape = MaterialShapes.Clover8Leaf.toShape()

    val infiniteTransition = rememberInfiniteTransition(label = "cloveRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloveRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tune Your Vibe",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FunnelDisplayFamily
            ),
            modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        )

        Text(
            text = selectedMood.subtitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.animateContentSize()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CloveShape)
        ) {
            Crossfade(
                targetState = selectedMood,
                animationSpec = tween(400),
                label = "imageFade"
            ) { mood ->
                Image(
                    painter = painterResource(
                        id = mood.drawableId
                    ),
                    contentDescription = mood.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CloveShape)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        shape = CloveShape
                    )
                    .graphicsLayer(rotationZ = rotation)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Slider(
                value = selectedMoodIndex.toFloat(),
                onValueChange = { newIndex ->
                    selectedMoodIndex = newIndex.toInt()
                },
                valueRange = 0f..2f,
                steps = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onBackground,
                    activeTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onBackground)
                            .shadow(elevation = 4.dp)
                    )
                },
                track = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = moods[0].label,
                    color = if (selectedMoodIndex == 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = if (selectedMoodIndex == 0) FontWeight.SemiBold else FontWeight.Normal
                )

                Text(
                    text = moods[2].label,
                    color = if (selectedMoodIndex == 2) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = if (selectedMoodIndex == 2) FontWeight.SemiBold else FontWeight.Normal
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