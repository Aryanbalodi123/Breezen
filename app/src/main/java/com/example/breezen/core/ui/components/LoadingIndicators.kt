package com.example.breezen.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun LoadingPillsIndicator(
    modifier: Modifier = Modifier,
    pillColor: Color = MaterialTheme.colorScheme.primary,
    pillCount: Int = 4,
    animationDuration: Int = 600,
    minHeight: Dp = 12.dp,
    maxHeight: Dp = 40.dp,
    pillWidth: Dp = 8.dp,
    spacing: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pills")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pillCount) { index ->
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = minHeight.value,
                targetValue = maxHeight.value,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDuration, easing = FastOutSlowInEasing
                    ), repeatMode = RepeatMode.Reverse, initialStartOffset = StartOffset(
                        offsetMillis = (animationDuration / pillCount) * index
                    )
                ),
                label = "pill_height_$index"
            )

            Box(
                modifier = Modifier
                    .width(pillWidth)
                    .height(animatedHeight.dp)
                    .background(
                        color = pillColor, shape = RoundedCornerShape(pillWidth / 2)
                    )
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(pillWidth / 2),
                        ambientColor = pillColor.copy(alpha = 0.3f),
                        spotColor = pillColor.copy(alpha = 0.3f)
                    )
            )
        }
    }
}