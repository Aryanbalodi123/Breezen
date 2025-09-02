package com.example.askquestion.ui.screens

import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.askquestion.R
import com.example.askquestion.network.IMAGE_BUCKET_URL
import com.example.askquestion.theme.AppColors
import com.example.askquestion.theme.CustomTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.*

fun minSec(duration: Long): List<Long> {
    val minutes = duration / 1000 / 60
    val seconds = (duration / 1000) % 60
    return listOf(minutes, seconds)
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

sealed class PlayerEvent{
    object PlayPause: PlayerEvent()
    object Previous: PlayerEvent()
    object Next: PlayerEvent()
    object ToggleShuffle: PlayerEvent()
    object ToggleRepeat : PlayerEvent()
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(
    navController: NavHostController,
    viewModel: TabViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentSong = viewModel.currentSong
    if (currentSong == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    // --- Player: remembered and never set to null ---
    val player = remember {
        ExoPlayer.Builder(context).build()
    }


    // Prepare media when song/filePath changes
    LaunchedEffect(currentSong.id, viewModel.filePath) {
        try {
            player.setMediaItem(MediaItem.fromUri(viewModel.filePath))
            player.prepare()
            player.playWhenReady = true
        } catch (t: Throwable) {
            Log.e("PlayerScreen", "prepare failed", t)
        }
    }

    // Lifecycle: pause/resume and release on dispose
    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> player.playWhenReady = true
                Lifecycle.Event.ON_STOP -> player.playWhenReady = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                player.release()
            } catch (t: Throwable) {
                Log.e("PlayerScreen", "release failed", t)
            }
        }
    }

    // --- UI state (kept local where appropriate) ---
    var dominantColor by remember { mutableStateOf(Color(0xFF6366F1)) }
    val beigeColor = Color(0xFFF5F5DC)
    var currentTime by remember { mutableLongStateOf(0L) }
    var isLoadingPlayer by remember { mutableStateOf(false) }

    // --- Player listener + ticker (cancellation-safe) ---
    LaunchedEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isNowPlaying: Boolean) {
                // update viewModel state from listener
                viewModel.isPlaying = isNowPlaying
            }
            override fun onPlaybackStateChanged(state: Int) {
                isLoadingPlayer = (state == Player.STATE_BUFFERING)
            }
        }
        player.addListener(listener)

        try {
            while (isActive) {
                try {
                    currentTime = player.currentPosition
                } catch (t: Throwable) {
                    break
                }
                delay(500L)
            }
        } finally {
            player.removeListener(listener)
        }
    }

    // --- Palette extraction ---
    LaunchedEffect(currentSong.id) {
        try {
            val request = ImageRequest.Builder(context)
                .data(IMAGE_BUCKET_URL + currentSong.id + ".webp")
                .allowHardware(false)
                .build()
            val result = (context.imageLoader.execute(request) as? SuccessResult)?.drawable
            result?.let { drawable ->
                Palette.from(drawable.toBitmap()).generate { palette ->
                    palette?.getDominantColor(0xFF444444.toInt())?.let { c ->
                        dominantColor = Color(c)
                    }
                }
            }
        } catch (ce: CancellationException) {
            /* cancelled -> ignore */
        } catch (t: Throwable) {
            Log.e("PlayerScreen", "palette failed", t)
            dominantColor = Color(0xFF6366F1)
        }
    }

    // --- UI layout (kept your structure) ---
    val songDurationMs = if (currentSong.duration > 0) currentSong.duration * 1000L else 1000L
    val (minuteTotal, secondTotal) = minSec(songDurationMs)
    val (minuteCurrent, secondCurrent) = minSec(currentTime)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .drawBehind { drawSunshineEffect(dominantColor, size) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Top bar (same as you had)
            Box(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.material3.IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Now Playing",
                    color = Color.White,
                    style = CustomTypography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Box(modifier = Modifier.align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
                MusicProgress(
                    currentTime = currentTime,
                    duration = songDurationMs,
                    strokeColor = beigeColor,
                    onSeek = { seekTime ->
                        coroutineScope.launch {
                            try {
                                player.seekTo(seekTime)
                                currentTime = seekTime
                            } catch (t: Throwable) {
                                Log.e("PlayerScreen", "seek failed", t)
                            }
                        }
                    }
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) {
                            append(String.format("%02d", minuteCurrent))
                            append(":")
                            append(String.format("%02d", secondCurrent))
                        }
                        append(" | ")
                        withStyle(SpanStyle(color = beigeColor)) {
                            append(String.format("%02d", minuteTotal))
                            append(":")
                            append(String.format("%02d", secondTotal))
                        }
                    },
                    style = CustomTypography.bodySmall,
                    modifier = Modifier.offset(y = (-125).dp)
                )

                AsyncImage(
                    model = IMAGE_BUCKET_URL + currentSong.id + ".webp",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(220.dp).clip(RoundedCornerShape(110.dp))
                )

                if (isLoadingPlayer) {
                    Box(
                        modifier = Modifier.size(220.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(110.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = beigeColor, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = currentSong.title, color = AppColors.TextPrimary, style = CustomTypography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(text = currentSong.artist ?: "Unknown", color = Color.White.copy(alpha = 0.7f), style = CustomTypography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(48.dp))

            // --- Controls: run events in coroutineScope to avoid synchronous composition-side effects ---
            MusicPlayerControls(
                isPlaying = viewModel.isPlaying,
                isShuffleEnabled = viewModel.isShuffleEnabled,
                repeatMode = viewModel.repeatMode,
                onEvent = { event ->
                    coroutineScope.launch {
                        when (event) {
                            PlayerEvent.PlayPause -> {
                                try {
                                    if (player.isPlaying) player.pause() else player.play()
                                    // isPlaying will be updated by listener above
                                } catch (t: Throwable) { Log.e("PlayerScreen", "play/pause failed", t) }
                            }
                            PlayerEvent.Next -> try { player.seekToNext() } catch (t: Throwable) { Log.e("PlayerScreen", "next failed", t) }
                            PlayerEvent.Previous -> try { player.seekToPrevious() } catch (t: Throwable) { Log.e("PlayerScreen", "prev failed", t) }
                            PlayerEvent.ToggleShuffle -> {
                                viewModel.isShuffleEnabled = !viewModel.isShuffleEnabled
                                try { player.shuffleModeEnabled = viewModel.isShuffleEnabled } catch (t: Throwable) { Log.e("PlayerScreen", "shuffle failed", t) }
                            }
                            PlayerEvent.ToggleRepeat -> {
                                // cycle viewModel.repeatMode
                                viewModel.repeatMode = when (viewModel.repeatMode) {
                                    TabViewModel.RepeatMode.OFF -> TabViewModel.RepeatMode.ALL
                                    TabViewModel.RepeatMode.ALL -> TabViewModel.RepeatMode.ONE
                                    TabViewModel.RepeatMode.ONE -> TabViewModel.RepeatMode.OFF
                                }

                                // apply to ExoPlayer
                                val exoRepeat = when (viewModel.repeatMode) {
                                    TabViewModel.RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                                    TabViewModel.RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                                    TabViewModel.RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                                }

                                player.repeatMode = exoRepeat


                            try { player.repeatMode = exoRepeat as Int
                                } catch (t: Throwable) { Log.e("PlayerScreen", "repeat failed", t) }
                            }
                        }
                    }
                }
            )
        }
    }
}

// Extension function to draw smooth sunshine effect from outside top-left
private fun DrawScope.drawSunshineEffect(dominantColor: Color, canvasSize: Size) {
    // Light source positioned outside the screen (top-left)
    val lightSource = Offset(-canvasSize.width * 0.3f, -canvasSize.height * 0.2f)

    // Create smooth, subtle light wash
    val maxDistance = kotlin.math.sqrt(
        (canvasSize.width * canvasSize.width + canvasSize.height * canvasSize.height).toDouble()
    ).toFloat() * 1.5f

    // Main diagonal light wash
    val mainGradient = Brush.linearGradient(
        colors = listOf(
            dominantColor.copy(alpha = 0.3f),
            dominantColor.copy(alpha = 0.2f),
            dominantColor.copy(alpha = 0.12f),
            dominantColor.copy(alpha = 0.08f),
            dominantColor.copy(alpha = 0.04f),
            Color.Transparent
        ),
        start = lightSource,
        end = Offset(canvasSize.width * 0.8f, canvasSize.height * 0.9f)
    )

    drawRect(
        brush = mainGradient,
        size = canvasSize
    )

    // Additional soft angular light
    val secondaryGradient = Brush.linearGradient(
        colors = listOf(
            dominantColor.copy(alpha = 0.15f),
            dominantColor.copy(alpha = 0.08f),
            dominantColor.copy(alpha = 0.04f),
            Color.Transparent
        ),
        start = Offset(-canvasSize.width * 0.2f, -canvasSize.height * 0.1f),
        end = Offset(canvasSize.width * 0.6f, canvasSize.height * 0.7f)
    )

    drawRect(
        brush = secondaryGradient,
        size = canvasSize
    )

    // Subtle radial highlight from the light source direction
    val radialGradient = Brush.radialGradient(
        colors = listOf(
            dominantColor.copy(alpha = 0.1f),
            dominantColor.copy(alpha = 0.06f),
            dominantColor.copy(alpha = 0.03f),
            Color.Transparent
        ),
        center = Offset(canvasSize.width * 0.15f, canvasSize.height * 0.1f),
        radius = canvasSize.width * 0.8f
    )

    drawCircle(
        brush = radialGradient,
        center = Offset(canvasSize.width * 0.15f, canvasSize.height * 0.1f),
        radius = canvasSize.width * 0.8f
    )
}

@Composable
fun MusicProgress(
    currentTime: Long,
    duration: Long,
    strokeColor: Color,
    onSeek: (Long) -> Unit
) {
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
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )

        // Background arc
        drawArc(
            color = strokeColor.copy(alpha = 0.3f),
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Glow arc
        drawArc(
            color = strokeColor.copy(alpha = 0.15f),
            startAngle = start,
            sweepAngle = sweep * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round)
        )

        // Main arc
        drawArc(
            color = strokeColor,
            startAngle = start,
            sweepAngle = sweep * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw tip
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
    val strokeWidth = 4f
    val cutAngle = 60f
    val sweep = 360f - cutAngle
    val startAngle = 270f + (cutAngle / 2f)

    val center = Offset(size.width / 2f, size.height / 2f)
    val touchVector = offset - center
    val distance = touchVector.getDistance()

    val radius = min(size.width, size.height) / 2f
    val innerRadius = radius - strokeWidth * 2f

    return if (distance in innerRadius..radius) {
        val angle = (Math.toDegrees(
            atan2(touchVector.y.toDouble(), touchVector.x.toDouble())
        ).toFloat() + 360f) % 360f

        val relative = (angle - startAngle + 360f) % 360f
        if (relative <= sweep) {
            (relative / sweep).coerceIn(0f, 1f)
        } else 0f
    } else 0f
}

@Composable
fun MusicPlayerControls(
    isPlaying: Boolean = false,
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
        // Shuffle Button
        IconButton(
            onClick = {onEvent(PlayerEvent.ToggleShuffle)},

        ) {
            Icon(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = "Toggle shuffle",
                modifier = Modifier.size(24.dp),
                tint = if (isShuffleEnabled) Color.Yellow else Color.White.copy(alpha = 0.7f)
            )
        }

        // Previous Button
        IconButton(
            onClick = {onEvent(PlayerEvent.Previous)},
        ) {
            Icon(
                painter = painterResource(R.drawable.set_backward),
                contentDescription = "Previous track",
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        }

        // Play/Pause Button
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable {onEvent(PlayerEvent.PlayPause)},
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    if (isPlaying) R.drawable.pause else R.drawable.play
                ),
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }

        // Next Button
        IconButton(
            onClick = {onEvent(PlayerEvent.Next)},
        ) {
            Icon(
                painter = painterResource(R.drawable.set_forward),
                contentDescription = "Next track",
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        }

        // Repeat Button
        IconButton(
            onClick = {onEvent(PlayerEvent.ToggleRepeat)},
        ) {
            Icon(
                painter = painterResource(R.drawable.repeat),
                contentDescription = "Toggle repeat mode",
                modifier = Modifier.size(24.dp),
                tint = when (repeatMode) {
                    TabViewModel.RepeatMode.OFF -> Color.White.copy(alpha = 0.7f)
                    TabViewModel.RepeatMode.ALL, TabViewModel.RepeatMode.ONE -> Color.Yellow
                }
            )
        }
    }
}

