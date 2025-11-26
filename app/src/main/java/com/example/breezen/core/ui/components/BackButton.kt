package com.example.breezen.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BackButton(
    navController: NavController,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    dotColor: Color = Color.White,   // green dashed ring
    arrowColor: Color = Color.White,
    iconSize: Dp = 20.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value

    // smooth press-down animation
    val scale = animateFloatAsState(if (isPressed) 0.95f else 1f).value

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current, // modern ripple
                onClick = { navController.popBackStack() }
            ),
        contentAlignment = Alignment.Center
    ) {

        // Dashed circular border
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.8.dp.toPx()
            val radius = (size.toPx() / 2) - strokeWidth

            drawCircle(
                color = dotColor,
                radius = radius,
                center = Offset(size.toPx() / 2, size.toPx() / 2),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(6.dp.toPx(), 6.dp.toPx())
                    )
                )
            )
        }

        // Arrow
        Icon(
            imageVector = Icons.Filled.ArrowBackIosNew,
            contentDescription = "Back",
            tint = arrowColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
