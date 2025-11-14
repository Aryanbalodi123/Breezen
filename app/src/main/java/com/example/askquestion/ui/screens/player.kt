package com.example.askquestion.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.askquestion.R
import com.example.askquestion.network.IMAGE_BUCKET_URL
import com.example.askquestion.network.Song
import com.example.askquestion.theme.AppColors
import com.example.askquestion.theme.CustomTypography
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

fun minSec(duration: Long): List<Long> {
    val minutes = duration / 1000 / 60
    val seconds = (duration / 1000) % 60
    return listOf(minutes, seconds)
}

sealed class PlayerEvent {
    object PlayPause : PlayerEvent()
    object Previous : PlayerEvent()
    object Next : PlayerEvent()
    object ToggleShuffle : PlayerEvent()
    object ToggleRepeat : PlayerEvent()
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    navController: NavHostController,
    viewModel: TabViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.playerUiState
    val currentSong = uiState.currentSong
    val hazeState = rememberHazeState()

    var isImageAndPaletteLoaded by remember { mutableStateOf(false) }
    var currentTime by remember { mutableLongStateOf(0L) }

    val player = remember { ExoPlayer.Builder(context).build() }

    val dominantColor by animateColorAsState(
        targetValue = uiState.dominantColor,
        animationSpec = tween(500),
        label = "dominantColor"
    )

    // --- Player Setup & Lifecycle ---
    LaunchedEffect(uiState.streamUrl) {
        if (uiState.streamUrl.isNotEmpty()) {
            try {
                player.setMediaItem(MediaItem.fromUri(uiState.streamUrl))
                player.prepare()
                player.playWhenReady = true
            } catch (t: Throwable) {
                Log.e("PlayerScreen", "prepare failed", t)
            }
        }
    }

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

    // --- Player Listener & State Ticker ---
    LaunchedEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isNowPlaying: Boolean) {
                viewModel.isPlaying = isNowPlaying
            }

            override fun onPlaybackStateChanged(state: Int) {
                viewModel.setIsBuffering(state == Player.STATE_BUFFERING)
                if (state == Player.STATE_READY && isImageAndPaletteLoaded) {
                    viewModel.onPlayerReadyAndImageLoaded()
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                    if (player.playbackState == Player.STATE_ENDED) {
                        viewModel.playNextSong(context)
                    }
                }
            }
        }
        player.addListener(listener)

        try {
            while (isActive) {
                try {
                    currentTime = player.currentPosition
                } catch (t: Throwable) {
                    break // Player released
                }
                delay(500L)
            }
        } finally {
            player.removeListener(listener)
        }
    }

    // --- Palette Extraction ---
    LaunchedEffect(currentSong?.id) {
        if (currentSong == null) return@LaunchedEffect
        isImageAndPaletteLoaded = false
        try {
            val request = ImageRequest.Builder(context)
                .data(IMAGE_BUCKET_URL + currentSong.id + ".webp")
                .allowHardware(false)
                .build()
            val result = (context.imageLoader.execute(request) as? SuccessResult)?.drawable
            val color = result?.let { drawable ->
                Palette.from(drawable.toBitmap()).generate()
                    ?.getDominantColor(0xFF444444.toInt())
                    ?.let { Color(it) }
            } ?: Color(0xFF6366F1) // Fallback
            viewModel.setDominantColor(color)
        } catch (t: Throwable) {
            Log.e("PlayerScreen", "palette failed", t)
            viewModel.setDominantColor(Color(0xFF6366F1)) // Fallback
        } finally {
            isImageAndPaletteLoaded = true
            if (player.playbackState == Player.STATE_READY) {
                viewModel.onPlayerReadyAndImageLoaded()
            }
        }
    }

    // --- UI Layering ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Layer 1: The Player Content (always present, provides blur source)
        PlayerContent(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState), // This content will be blurred by Haze
            hazeState = hazeState, // Pass HazeState to the new "Next Up" bar
            uiState = uiState,
            dominantColor = dominantColor,
            currentTime = currentTime,
            isPlaying = viewModel.isPlaying,
            isShuffleEnabled = viewModel.isShuffleEnabled,
            repeatMode = viewModel.repeatMode,
            onSeek = { seekTime ->
                try {
                    player.seekTo(seekTime)
                    currentTime = seekTime
                } catch (t: Throwable) {
                    Log.e("PlayerScreen", "seek failed", t)
                }
            },
            onEvent = { event ->
                when (event) {
                    PlayerEvent.PlayPause -> {
                        if (player.isPlaying) player.pause() else player.play()
                    }
                    PlayerEvent.Next -> viewModel.playNextSong(context)
                    PlayerEvent.Previous -> viewModel.playPreviousSong(context)
                    PlayerEvent.ToggleShuffle -> viewModel.toggleShuffle()
                    PlayerEvent.ToggleRepeat -> viewModel.toggleRepeat()
                }
            },
            onBack = { navController.popBackStack() }
        )

        // Layer 2: Transition Loader (Pulse)
        AnimatedVisibility(
            visible = uiState.loadState == PlayerLoadState.TRANSITIONING,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            PlayerTransitionLoader(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(
                        hazeState,
                        style = HazeStyle(
                            blurRadius = 30.dp,
                            tint = HazeTint(Color.Black.copy(alpha = 0.5f))
                        )
                    )
            )
        }

        // Layer 3: Initial Loader (Plant)
        AnimatedVisibility(
            visible = uiState.loadState == PlayerLoadState.INITIAL,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            PlayerInitialLoadScreen()
        }
    }
}

@Composable
fun PlayerInitialLoadScreen() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.plant_loader))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051f05)), // Very dark green
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(250.dp)
        )
    }
}

@Composable
fun PlayerTransitionLoader(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.pulse_loader))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    Box(
        modifier = modifier.fillMaxSize(), // Haze/blur is applied via modifier
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(250.dp)
        )
    }
}


/**
 * 🔥 REDESIGN: This composable holds the actual player UI.
 * Layout is changed: Controls are now under the title.
 * "Next Up" bar is at the bottom.
 */
@Composable
fun PlayerContent(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    uiState: PlayerUiState,
    dominantColor: Color,
    currentTime: Long,
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: TabViewModel.RepeatMode,
    onSeek: (Long) -> Unit,
    onEvent: (PlayerEvent) -> Unit,
    onBack: () -> Unit
) {
    val currentSong = uiState.currentSong
    val beigeColor = Color(0xFFF5F5DC)

    val songDurationMs = if (currentSong?.duration ?: 0 > 0) currentSong!!.duration * 1000L else 1000L
    val (minuteTotal, secondTotal) = minSec(songDurationMs)
    val (minuteCurrent, secondCurrent) = minSec(currentTime)

    Box(
        modifier = modifier
            .background(Color.Black)
            .drawBehind { drawSunshineEffect(dominantColor, size) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔥 REDESIGN: Top bar is cleaner. "Now Playing" is removed.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                IconButton(
                    onClick = onBack,
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
                // "Now Playing" Text is removed for a minimal look
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Progress + Image
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                MusicProgress(
                    currentTime = currentTime,
                    duration = songDurationMs,
                    strokeColor = beigeColor,
                    onSeek = onSeek
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
                    model = IMAGE_BUCKET_URL + (currentSong?.id ?: "") + ".webp",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(110.dp))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = currentSong?.title ?: "Loading...",
                color = AppColors.TextPrimary,
                style = CustomTypography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Artist
            Text(
                text = currentSong?.artist ?: "Unknown",
                color = AppColors.TextSecondary,
                style = CustomTypography.bodyLarge.copy(
                    fontSize = 18.sp
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(32.dp)) // "Expensive" spacing

            // 🔥 REDESIGN: Controls are moved UP
            MusicPlayerControls(
                isPlaying = isPlaying,
                isBuffering = uiState.isBuffering,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onEvent = onEvent
            )

            // This spacer pushes the new "Next Up" bar to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // 🔥 REDESIGN: New "Next Up" bar at the bottom
            NextUpCard(
                hazeState = hazeState,
                nextUpSong = uiState.nextUpSong
            )

            Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
        }
    }
}

/**
 * 🔥 REDESIGN: New "Next Up" bar with iOS-style glassmorphism.
 */
@Composable
fun NextUpCard(
    hazeState: HazeState,
    nextUpSong: Song?
) {
    AnimatedVisibility(
        visible = nextUpSong != null,
        enter = fadeIn(animationSpec = tween(600)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp) // Main padding for the bar
    ) {
        if (nextUpSong == null) return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)) // Smooth rounded corners
                // The Haze effect for frosted glass
                .hazeEffect(
                    hazeState,
                    style = HazeStyle(
                        blurRadius = 25.dp,
                        tint = HazeTint(Color.White.copy(alpha = 0.15f))
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(20.dp)
                )
                .padding(12.dp) // Inner padding
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Next song's image
                AsyncImage(
                    model = IMAGE_BUCKET_URL + nextUpSong.id + ".webp",
                    contentDescription = "Next track cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Text column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // "Next Up" label with neon-green accent
                    Text(
                        text = "NEXT UP",
                        style = CustomTypography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = AppColors.PrimaryGreen,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = nextUpSong.title,
                        style = CustomTypography.bodyMedium,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = nextUpSong.artist ?: "Unknown",
                        style = CustomTypography.bodySmall,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


// --- Utility and Control Composables ---

private fun DrawScope.drawSunshineEffect(dominantColor: Color, canvasSize: Size) {
    val lightSource = Offset(-canvasSize.width * 0.3f, -canvasSize.height * 0.2f)
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
    drawRect(brush = mainGradient, size = canvasSize)

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
    drawRect(brush = secondaryGradient, size = canvasSize)
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
                    // 🔥 FIX: Pass the tap offset directly to the seek logic
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

/**
 * 🔥 FIX: Seek logic is simplified.
 * Removed the 'if (distance in innerRadius..radius)' check.
 * Now, tapping anywhere in the circle seeks based on the angle,
 * which is far more intuitive and fixes the "glitchy" feel.
 */
private fun calculateProgressFromOffset(offset: Offset, size: IntSize): Float {
    val cutAngle = 60f
    val sweep = 360f - cutAngle
    val startAngle = 270f + (cutAngle / 2f)

    val center = Offset(size.width / 2f, size.height / 2f)
    val touchVector = offset - center

    // Calculate the angle of the tap relative to the center
    val angle = (Math.toDegrees(
        atan2(touchVector.y.toDouble(), touchVector.x.toDouble())
    ).toFloat() + 360f) % 360f

    // Convert the angle to a progress value (0f to 1f)
    val relative = (angle - startAngle + 360f) % 360f
    return if (relative <= sweep) {
        (relative / sweep).coerceIn(0f, 1f)
    } else {
        // Handle taps in the "cut" section
        if (angle > startAngle + sweep || angle < startAngle) {
            if (relative > (360f - cutAngle / 2f)) 0f else 1f
        } else {
            0f
        }
    }
}

@Composable
fun MusicPlayerControls(
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
                tint = if (isShuffleEnabled) AppColors.PrimaryGreen else Color.White.copy(alpha = 0.7f)
            )
        }
        IconButton(onClick = { onEvent(PlayerEvent.Previous) }) {
            Icon(
                painter = painterResource(R.drawable.set_backward),
                contentDescription = "Previous track",
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onEvent(PlayerEvent.PlayPause) },
            contentAlignment = Alignment.Center
        ) {
            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color.Black,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    painter = painterResource(
                        if (isPlaying) R.drawable.pause else R.drawable.play
                    ),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        IconButton(onClick = { onEvent(PlayerEvent.Next) }) {
            Icon(
                painter = painterResource(R.drawable.set_forward),
                contentDescription = "Next track",
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        }
        IconButton(onClick = { onEvent(PlayerEvent.ToggleRepeat) }) {
            Icon(
                painter = painterResource(
                    if (repeatMode == TabViewModel.RepeatMode.ONE) R.drawable.repeat_one else R.drawable.repeat
                ),
                contentDescription = "Toggle repeat mode",
                modifier = Modifier.size(24.dp),
                tint = when (repeatMode) {
                    TabViewModel.RepeatMode.OFF -> Color.White.copy(alpha = 0.7f)
                    TabViewModel.RepeatMode.ALL, TabViewModel.RepeatMode.ONE -> AppColors.PrimaryGreen
                }
            )
        }
    }
}