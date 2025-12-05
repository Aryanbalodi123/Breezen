package com.example.breezen.feature.player.components

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
import com.example.breezen.R
import com.example.breezen.feature.music.TabViewModel
import com.example.breezen.feature.player.PlayerEvent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Renders a circular progress bar with a specific "cut" (gap) at the bottom.
 * Handles touch gestures to seek to a specific timestamp in the track.
 *
 * @param currentTime Current playback position in milliseconds.
 * @param duration Total track duration in milliseconds.
 * @param strokeColor The primary color of the progress arc.
 * @param onSeek Callback triggered when the user taps on the arc.
 */
@Composable
internal fun MusicProgress(
    currentTime: Long,
    duration: Long,
    strokeColor: Color,
    onSeek: (Long) -> Unit
) {
    // Normalize progress to 0.0 - 1.0 range, guarding against division by zero
    val progress = if (duration > 0) (currentTime.toFloat() / duration).coerceIn(0f, 1f) else 0f

    Canvas(
        modifier = Modifier
            .size(280.dp)
            .padding(15.dp)
            .pointerInput(Unit) {
                // Detect tap gestures to handle seeking functionality
                detectTapGestures { offset ->
                    val newProgress = calculateProgressFromOffset(offset, size)
                    onSeek((newProgress * duration).toLong())
                }
            }
    ) {
        // --- Geometry Configuration ---
        val strokeWidth = 4.dp.toPx()
        val cutAngle = 60f // The size of the gap at the bottom of the circle (in degrees)
        val sweep = 360f - cutAngle // The total active arc length
        val start = 270f + (cutAngle / 2f) // Start angle adjusted to center the gap at the bottom (90 degrees / 6 o'clock)

        val diameter = min(size.width, size.height)
        val arcSize = Size(diameter, diameter)

        // Center the arc within the canvas bounds
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

        // 1. Draw Background Track (Inactive portion)
        drawArc(
            color = strokeColor.copy(alpha = 0.3f),
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 2. Draw Glow Effect (Slightly wider, low opacity behind the active progress)
        drawArc(
            color = strokeColor.copy(alpha = 0.15f),
            startAngle = start,
            sweepAngle = sweep * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round)
        )

        // 3. Draw Active Progress
        drawArc(
            color = strokeColor,
            startAngle = start,
            sweepAngle = sweep * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 4. Draw the "Thumb" (Indicator tip)
        if (progress > 0f) {
            // Calculate the exact angle of the tip based on current progress
            val currentAngle = start + (sweep * progress)
            val angleInRadian = Math.toRadians(currentAngle.toDouble())

            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val arcRadius = diameter / 2f

            // Polar to Cartesian coordinate conversion
            val tipX = centerX + (arcRadius * cos(angleInRadian)).toFloat()
            val tipY = centerY + (arcRadius * sin(angleInRadian)).toFloat()

            // Draw outer glow for the thumb
            drawCircle(
                color = strokeColor.copy(alpha = 0.12f),
                radius = (strokeWidth / 2f + 2.dp.toPx()) * 1.4f,
                center = Offset(tipX, tipY)
            )
            // Draw solid thumb
            drawCircle(
                color = strokeColor,
                radius = strokeWidth / 2f + 2.dp.toPx(),
                center = Offset(tipX, tipY)
            )
        }
    }
}

/**
 * Calculates the progress (0.0 to 1.0) based on the touch coordinates relative to the center.
 * Uses atan2 to determine the angle of the touch and maps it to the progress arc.
 */
private fun calculateProgressFromOffset(offset: Offset, size: IntSize): Float {
    val cutAngle = 60f
    val sweep = 360f - cutAngle
    val startAngle = 270f + (cutAngle / 2f)

    val center = Offset(size.width / 2f, size.height / 2f)
    val touchVector = offset - center

    // Calculate angle of touch in degrees (0-360)
    // atan2 returns radians, we convert to degrees and normalize negative values
    val angle = (Math.toDegrees(atan2(touchVector.y.toDouble(), touchVector.x.toDouble()))
        .toFloat() + 360f) % 360f

    // Calculate angle relative to the start of our arc
    val relative = (angle - startAngle + 360f) % 360f

    return if (relative <= sweep) {
        // Touch is within the valid arc area
        (relative / sweep).coerceIn(0f, 1f)
    } else {
        // Touch is in the "gap" area. Snap to 0% or 100% depending on which side is closer.
        if (angle > startAngle + sweep || angle < startAngle) {
            if (relative > (360f - cutAngle / 2f)) 0f else 1f
        } else {
            0f
        }
    }
}

/**
 * Displays the main playback controls (Shuffle, Previous, Play/Pause, Next, Repeat).
 * Handles loading states (buffering) and icon toggling based on Repeat/Shuffle modes.
 */
@Composable
internal fun MusicPlayerControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isBuffering: Boolean = false,
    isShuffleEnabled: Boolean = false,
    repeatMode: TabViewModel.RepeatMode = TabViewModel.RepeatMode.OFF,
    onEvent: (PlayerEvent) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        // Shuffle Button
        IconButton(onClick = { onEvent(PlayerEvent.ToggleShuffle) }) {
            Icon(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = "Toggle shuffle",
                modifier = Modifier.size(24.dp),
                // Highlight color when active, dimmed when inactive
                tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Previous Track Button
        IconButton(onClick = { onEvent(PlayerEvent.Previous) }) {
            Icon(
                painter = painterResource(R.drawable.set_backward),
                contentDescription = "Previous track",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        // Play/Pause/Buffer Button Container
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onBackground)
                .clickable { onEvent(PlayerEvent.PlayPause) },
            contentAlignment = Alignment.Center
        ) {
            if (isBuffering) {
                // Show loader if buffering, ignoring play/pause state
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.background,
                    strokeWidth = 3.dp
                )
            } else {
                // Show Play or Pause icon
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Next Track Button
        IconButton(onClick = { onEvent(PlayerEvent.Next) }) {
            Icon(
                painter = painterResource(R.drawable.set_forward),
                contentDescription = "Next track",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        // Repeat Mode Button (Off -> All -> One)
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