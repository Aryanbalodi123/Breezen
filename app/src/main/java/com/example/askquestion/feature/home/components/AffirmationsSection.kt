package com.example.askquestion.feature.home.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.askquestion.R
import com.example.askquestion.core.ui.theme.FunnelDisplayFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Affirmation(
    val id: Int,
    val text: String,
    @DrawableRes val backgroundResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffirmationSection() {
    val affirmations = remember {
        mutableStateListOf(
            Affirmation(1, "I am capable of achieving my goals", R.drawable.affirmation_card_01),
            Affirmation(
                2,
                "I choose to be happy and love myself today",
                R.drawable.affirmation_card_02
            ),
            Affirmation(3, "My potential to succeed is infinite", R.drawable.affirmation_card_03),
            Affirmation(
                4,
                "I am resilient and can handle anything",
                R.drawable.affirmation_card_04
            ),
            Affirmation(
                5,
                "I radiate positivity and attract good things",
                R.drawable.affirmation_card_05
            ),
            Affirmation(6, "Today I choose joy and gratitude", R.drawable.affirmation_card_06),
            Affirmation(7, "I am worthy of love and respect", R.drawable.affirmation_card_07),
            Affirmation(8, "I trust in my journey and timing", R.drawable.affirmation_card_08)
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (affirmations.isEmpty()) {
                Text(
                    text = "You've gone through all affirmations for today!",
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                affirmations.forEachIndexed { index, affirmation ->
                    if (index >= affirmations.size - 3) {
                        val stackIndex = affirmations.size - 1 - index
                        val isTopCard = index == affirmations.size - 1

                        AffirmationCard(
                            affirmation = affirmation,
                            isTopCard = isTopCard,
                            modifier = Modifier
                                .offset(y = (stackIndex * 12).dp)
                                .graphicsLayer {
                                    scaleX = 1f - (stackIndex * 0.04f)
                                    scaleY = 1f - (stackIndex * 0.04f)
                                    alpha = 1f - (stackIndex * 0.2f)
                                }
                                .padding(horizontal = (stackIndex * 12).dp),
                            onSwipe = {
                                affirmations.remove(affirmation)
                            }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffirmationCard(
    affirmation: Affirmation,
    isTopCard: Boolean,
    modifier: Modifier = Modifier,
    onSwipe: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    val screenWidthPx =
        with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val dismissThreshold = screenWidthPx * 0.4f

    val cardModifier = if (isTopCard) {
        modifier.pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount.x)
                        offsetY.snapTo(offsetY.value + dragAmount.y)
                        rotation.snapTo((offsetX.value / screenWidthPx) * 20f)
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        val shouldDismiss = kotlin.math.abs(offsetX.value) > dismissThreshold

                        if (shouldDismiss) {
                            val targetX =
                                if (offsetX.value > 0) screenWidthPx * 1.5f else -screenWidthPx * 1.5f
                            launch {
                                offsetX.animateTo(
                                    targetValue = targetX,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }
                            launch {
                                offsetY.animateTo(
                                    targetValue = offsetY.value + 100f,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }
                            launch {
                                rotation.animateTo(
                                    targetValue = if (offsetX.value > 0) 30f else -30f,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }
                            delay(300)
                            onSwipe()
                        } else {
                            // Animate back to center
                            launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                rotation.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    }
                }
            )
        }
    } else {
        modifier
    }

    Box(
        modifier = cardModifier
            .offset(
                x = with(LocalDensity.current) { offsetX.value.toDp() },
                y = with(LocalDensity.current) { offsetY.value.toDp() }
            )
            .graphicsLayer {
                rotationZ = rotation.value
            }
            .height(240.dp)
            .fillMaxWidth()
            .padding(horizontal = 8.dp)

            .shadow(
                elevation = if (isTopCard) 12.dp else 4.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter = painterResource(id = affirmation.backgroundResId),
            contentDescription = "Affirmation card background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = affirmation.text,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FunnelDisplayFamily, // Apply creative font
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 28.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 24.dp)
            )
        }
    }
}