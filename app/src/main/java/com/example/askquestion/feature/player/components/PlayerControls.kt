package com.example.askquestion.feature.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.askquestion.R
import com.example.askquestion.feature.music.TabViewModel
import com.example.askquestion.feature.player.PlayerEvent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun MusicProgress(currentTime: Long, duration: Long, strokeColor: Color, onSeek: (Long) -> Unit) {
    val progress = if (duration > 0) (currentTime.toFloat() / duration).coerceIn(0f, 1f) else 0f

    Canvas(
        modifier = Modifier
            .size(280.dp)
            .padding(15.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = calculateProgressFromOffset(offset, size)
                    onSeek((newProgress * duration).toLong())
                }
            }
    ) {
        val strokeWidth = 4.dp.toPx()
        val cutAngle = 60f
        val sweep = 360f - cutAngle
        val start = 270f + (cutAngle / 2f)
        val diameter = min(size.width, size.height)
        val arcSize = Size(diameter, diameter)
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

        drawArc(
            color = strokeColor.copy(alpha = 0.3f),
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = strokeColor.copy(alpha = 0.15f),
            startAngle = start,
            sweepAngle = sweep * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round)
        )
        drawArc(
            color = strokeColor,
            startAngle = start,
            sweepAngle = sweep * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        if (progress > 0f) {
            val currentAngle = start + (sweep * progress)
            val angleInRadian = Math.toRadians(currentAngle.toDouble())
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val arcRadius = diameter / 2f
            val tipX = centerX + (arcRadius * cos(angleInRadian)).toFloat()
            val tipY = centerY + (arcRadius * sin(angleInRadian)).toFloat()
            drawCircle(
                color = strokeColor.copy(alpha = 0.12f),
                radius = (strokeWidth / 2f + 2.dp.toPx()) * 1.4f,
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = strokeColor,
                radius = strokeWidth / 2f + 2.dp.toPx(),
                center = Offset(tipX, tipY)
            )
        }
    }
}

private fun calculateProgressFromOffset(offset: Offset, size: IntSize): Float {
    val cutAngle = 60f
    val sweep = 360f - cutAngle
    val startAngle = 270f + (cutAngle / 2f)

    val center = Offset(size.width / 2f, size.height / 2f)
    val touchVector = offset - center

    val angle = (Math.toDegrees(atan2(touchVector.y.toDouble(), touchVector.x.toDouble()))
        .toFloat() + 360f) % 360f
    val relative = (angle - startAngle + 360f) % 360f
    return if (relative <= sweep) {
        (relative / sweep).coerceIn(0f, 1f)
    } else {
        if (angle > startAngle + sweep || angle < startAngle) {
            if (relative > (360f - cutAngle / 2f)) 0f else 1f
        } else {
            0f
        }
    }
}

@Composable
internal fun MusicPlayerControls(
    isPlaying: Boolean = false,
    isBuffering: Boolean = false,
    isShuffleEnabled: Boolean = false,
    repeatMode: TabViewModel.RepeatMode = TabViewModel.RepeatMode.OFF,
    onEvent: (PlayerEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { onEvent(PlayerEvent.ToggleShuffle) }) {
            Icon(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = "Toggle shuffle",
                modifier = Modifier.size(24.dp),
                tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        IconButton(onClick = { onEvent(PlayerEvent.Previous) }) {
            Icon(
                painter = painterResource(R.drawable.set_backward),
                contentDescription = "Previous track",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onBackground)
                .clickable { onEvent(PlayerEvent.PlayPause) }, contentAlignment = Alignment.Center
        ) {
            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.background,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        IconButton(onClick = { onEvent(PlayerEvent.Next) }) {
            Icon(
                painter = painterResource(R.drawable.set_forward),
                contentDescription = "Next track",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(onClick = { onEvent(PlayerEvent.ToggleRepeat) }) {
            Icon(
                painter = painterResource(if (repeatMode == TabViewModel.RepeatMode.ONE) R.drawable.repeat_one else R.drawable.repeat),
                contentDescription = "Toggle repeat mode",
                modifier = Modifier.size(24.dp),
                tint = when (repeatMode) {
                    TabViewModel.RepeatMode.OFF -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    TabViewModel.RepeatMode.ALL, TabViewModel.RepeatMode.ONE -> MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}