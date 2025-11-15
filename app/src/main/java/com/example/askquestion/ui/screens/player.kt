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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
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
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


// Utility
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

    // ───────────────────────────────────────────────────────────────
    // NEW FLAGS
    // ───────────────────────────────────────────────────────────────
    var isPlayerReady by remember { mutableStateOf(false) }
    var isImageLoaded by remember { mutableStateOf(false) }
    var currentTime by remember { mutableLongStateOf(0L) }

    val player = remember { ExoPlayer.Builder(context).build() }

    val dominantColor by animateColorAsState(
        targetValue = uiState.dominantColor,
        animationSpec = tween(500)
    )

    // ───────────────────────────────────────────────────────────────
    // PLAYER SETUP ON NEW STREAM URL
    // ───────────────────────────────────────────────────────────────
    LaunchedEffect(uiState.streamUrl) {
        if (uiState.streamUrl.isNotEmpty()) {
            isPlayerReady = false
            // isImageLoaded = false // 🔥 **BUG FIX**: DO NOT reset image flag here

            Log.d("PlayerScreen", "⏯ Setting up player for ${uiState.streamUrl}")

            try {
                player.stop()
                player.clearMediaItems()

                val item = MediaItem.Builder()
                    .setUri(uiState.streamUrl)
                    .setMediaId(uiState.streamUrl)
                    .build()

                player.setMediaItem(item)

                // 🔥 **FIX**: We no longer add the next media item here.
                // We will let the ViewModel control this via the listener.

                player.prepare()
                player.playWhenReady = true
            } catch (e: Exception) {
                Log.e("PlayerScreen", "Player prepare failed", e)
            }
        }
    }

    // ───────────────────────────────────────────────────────────────
    // LIFECYCLE
    // ───────────────────────────────────────────────────────────────
    DisposableEffect(lifecycleOwner, player) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> player.playWhenReady = true
                Lifecycle.Event.ON_STOP -> player.playWhenReady = false
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            player.release()
        }
    }

    // ───────────────────────────────────────────────────────────────
    // PLAYER LISTENER
    // ───────────────────────────────────────────────────────────────
    LaunchedEffect(player) {
        val listener = object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                viewModel.isPlaying = isPlaying
            }

            override fun onPlaybackStateChanged(state: Int) {
                viewModel.setIsBuffering(state == Player.STATE_BUFFERING)

                if (state == Player.STATE_READY) {
                    Log.d("PlayerScreen", "✔ Player READY")
                    isPlayerReady = true
                }

                // 🔥 **FIX**: When song ends, tell ViewModel to play next.
                if (state == Player.STATE_ENDED) {
                    Log.d("PlayerScreen", "Song ended, playing next")
                    viewModel.playNextSong(context, forceManual = false)
                }
            }

            // 🔥 **FIX**: Removed onMediaItemTransition.
            // This was causing the double loader.
        }

        player.addListener(listener)

        // Ticker for progress
        try {
            while (isActive) {
                try {
                    currentTime = player.currentPosition
                } catch (_: Exception) {}
                delay(500)
            }
        } finally {
            player.removeListener(listener)
        }
    }

    // ───────────────────────────────────────────────────────────────
    // IMAGE LOADING WITH TIMEOUT
    // ───────────────────────────────────────────────────────────────
    LaunchedEffect(currentSong?.id) {
        val id = currentSong?.id ?: return@LaunchedEffect
        isImageLoaded = false // <-- This is now the ONLY place this is set to false

        val success = withTimeoutOrNull(2000) {
            val request = ImageRequest.Builder(context)
                .data(IMAGE_BUCKET_URL + id + ".webp")
                .build()

            context.imageLoader.execute(request)
            true
        }

        isImageLoaded = true // <-- Image is now loaded (or timed out)

        Log.d(
            "PlayerScreen",
            if (success == true) "✔ Image Loaded" else "⚠ Timeout Image"
        )
    }

    // ───────────────────────────────────────────────────────────────
    // HIDE *INITIAL* LOADER WHEN BOTH READY
    // ───────────────────────────────────────────────────────────────
    LaunchedEffect(isPlayerReady, isImageLoaded) {
        if (isPlayerReady && isImageLoaded) {
            viewModel.onPlayerReadyAndImageLoaded()
        }
    }

    // ───────────────────────────────────────────────────────────────
    // UI LAYERS
    // ───────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // ───── UI CONTENT (blur source)
        PlayerContent(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
            hazeState = hazeState,
            uiState = uiState,
            isImageLoaded = isImageLoaded,
            dominantColor = dominantColor,
            currentTime = currentTime, // <-- Pass the real current time
            isPlaying = viewModel.isPlaying,
            isShuffleEnabled = viewModel.isShuffleEnabled,
            repeatMode = viewModel.repeatMode,
            onSeek = { t ->
                try {
                    player.seekTo(t)
                } catch (_: Exception) {}
            },
            onEvent = { event ->
                when (event) {
                    PlayerEvent.PlayPause ->
                        if (player.isPlaying) player.pause() else player.play()

                    // 🔥 **FIX**: Stop player *before* requesting next song
                    PlayerEvent.Next -> {
                        player.stop()
                        viewModel.playNextSong(context, forceManual = true)
                    }

                    // 🔥 **FIX**: Stop player *before* requesting previous song
                    PlayerEvent.Previous -> {
                        player.stop()
                        viewModel.playPreviousSong(context)
                    }

                    PlayerEvent.ToggleShuffle ->
                        viewModel.toggleShuffle()

                    PlayerEvent.ToggleRepeat ->
                        viewModel.toggleRepeat()
                }
            },
            onBack = { navController.popBackStack() }
        )

        // ───── INITIAL LOADER
        AnimatedVisibility(
            visible = uiState.loadState == PlayerLoadState.INITIAL,
            enter = fadeIn(),
            exit = fadeOut(tween(500))
        ) {
            PlayerInitialLoadScreen()
        }
    }
}


/* ----------------------
   PlayerInitialLoadScreen & Transition loader
   ---------------------- */
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
            .background(Color(0xFF051f05)),
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
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(250.dp))
    }
}

/* ----------------------
   PlayerContent + Controls + Helpers
   ---------------------- */

@Composable
fun PlayerContent(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    uiState: PlayerUiState,
    isImageLoaded: Boolean,
    dominantColor: Color,
    currentTime: Long, // <-- This is the *real* time from the player
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: TabViewModel.RepeatMode,
    onSeek: (Long) -> Unit,
    onEvent: (PlayerEvent) -> Unit,
    onBack: () -> Unit
) {
    val currentSong = uiState.currentSong
    val beigeColor = Color(0xFFF5F5DC)

    // 🔥 **FIX**: We show shimmer *only* if the image isn't loaded yet.
    // The player's buffering state (during seek) is handled by the
    // spinner on the play button, not by the whole UI.
    val showShimmer = !isImageLoaded

    // 🔥 **FIX**: Use 0L for progress and time text while shimmering
    val effectiveCurrentTime = if (showShimmer) 0L else currentTime

    val songDurationMs = if ((currentSong?.duration ?: 0) > 0) currentSong!!.duration * 1000L else 1000L
    val (minuteTotal, secondTotal) = minSec(songDurationMs)
    // 🔥 **FIX**: Use effectiveCurrentTime
    val (minuteCurrent, secondCurrent) = minSec(effectiveCurrentTime)

    Box(
        modifier = modifier
            .background(Color.Black)
            .drawBehind { drawSunshineEffect(dominantColor, size) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Progress + Image
            Box(modifier = Modifier.align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
                MusicProgress(
                    // 🔥 **FIX**: Use effectiveCurrentTime
                    currentTime = effectiveCurrentTime,
                    duration = songDurationMs,
                    strokeColor = beigeColor,
                    onSeek = onSeek
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) {
                            // 🔥 **FIX**: Use values from effectiveCurrentTime
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

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(110.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isImageLoaded) {
                        AsyncImage(
                            model = IMAGE_BUCKET_URL + (currentSong?.id ?: "") + ".webp",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Show shimmer while image is loading
                        ShimmerBox(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title with shimmer
            // 🔥 **FIX**: Logic now only depends on `showShimmer`
            if (showShimmer) {
                ShimmerBox(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth(0.7f)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Text(
                    text = currentSong?.title ?: "Loading...",
                    color = AppColors.TextPrimary,
                    style = CustomTypography.headlineLarge.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Artist with shimmer
            // 🔥 **FIX**: Logic now only depends on `showShimmer`
            if (showShimmer) {
                ShimmerBox(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Text(
                    text = currentSong?.artist ?: "Unknown",
                    color = AppColors.TextSecondary,
                    style = CustomTypography.bodyLarge.copy(fontSize = 18.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            MusicPlayerControls(
                isPlaying = isPlaying,
                isBuffering = uiState.isBuffering,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onEvent = onEvent
            )

            // 🔥 **UI CHANGE**: Moved NextUpCard here
            Spacer(modifier = Modifier.height(32.dp))
            NextUpCard(hazeState = hazeState, nextUpSong = uiState.nextUpSong)

            // 🔥 **UI CHANGE**: This spacer now pushes everything up
            Spacer(modifier = Modifier.weight(1f))

            // (Removed the Spacer(16.dp) from here)
        }
    }
}

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
fun MusicProgress(currentTime: Long, duration: Long, strokeColor: Color, onSeek: (Long) -> Unit) {
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
            drawCircle(color = strokeColor.copy(alpha = 0.12f), radius = (strokeWidth / 2f + 2.dp.toPx()) * 1.4f, center = Offset(tipX, tipY))
            drawCircle(color = strokeColor, radius = strokeWidth / 2f + 2.dp.toPx(), center = Offset(tipX, tipY))
        }
    }
}

private fun calculateProgressFromOffset(offset: Offset, size: IntSize): Float {
    val cutAngle = 60f
    val sweep = 360f - cutAngle
    val startAngle = 270f + (cutAngle / 2f)

    val center = Offset(size.width / 2f, size.height / 2f)
    val touchVector = offset - center

    val angle = (Math.toDegrees(atan2(touchVector.y.toDouble(), touchVector.x.toDouble())).toFloat() + 360f) % 360f
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
fun NextUpCard(hazeState: HazeState, nextUpSong: Song?) {
    AnimatedVisibility(visible = nextUpSong != null, enter = fadeIn(animationSpec = tween(600)), exit = fadeOut(animationSpec = tween(300))) {
        if (nextUpSong == null) return@AnimatedVisibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .hazeEffect(hazeState, style = HazeStyle(blurRadius = 25.dp, tint = HazeTint(Color.White.copy(alpha = 0.15f))))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = IMAGE_BUCKET_URL + nextUpSong.id + ".webp",
                    contentDescription = "Next track cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "NEXT UP", style = CustomTypography.bodySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = AppColors.PrimaryGreen, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = nextUpSong.title, style = CustomTypography.bodyMedium, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = nextUpSong.artist ?: "Unknown", style = CustomTypography.bodySmall, color = AppColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
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
    Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        IconButton(onClick = { onEvent(PlayerEvent.ToggleShuffle) }) {
            Icon(painter = painterResource(R.drawable.shuffle), contentDescription = "Toggle shuffle", modifier = Modifier.size(24.dp), tint = if (isShuffleEnabled) AppColors.PrimaryGreen else Color.White.copy(alpha = 0.7f))
        }
        IconButton(onClick = { onEvent(PlayerEvent.Previous) }) {
            Icon(painter = painterResource(R.drawable.set_backward), contentDescription = "Previous track", modifier = Modifier.size(28.dp), tint = Color.White)
        }
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White).clickable { onEvent(PlayerEvent.PlayPause) }, contentAlignment = Alignment.Center) {
            if (isBuffering) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Color.Black, strokeWidth = 3.dp)
            } else {
                Icon(painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play), contentDescription = if (isPlaying) "Pause" else "Play", tint = Color.Black, modifier = Modifier.size(32.dp))
            }
        }
        IconButton(onClick = { onEvent(PlayerEvent.Next) }) {
            Icon(painter = painterResource(R.drawable.set_forward), contentDescription = "Next track", modifier = Modifier.size(28.dp), tint = Color.White)
        }
        IconButton(onClick = { onEvent(PlayerEvent.ToggleRepeat) }) {
            Icon(painter = painterResource(if (repeatMode == TabViewModel.RepeatMode.ONE) R.drawable.repeat_one else R.drawable.repeat), contentDescription = "Toggle repeat mode", modifier = Modifier.size(24.dp), tint = when (repeatMode) {
                TabViewModel.RepeatMode.OFF -> Color.White.copy(alpha = 0.7f)
                TabViewModel.RepeatMode.ALL, TabViewModel.RepeatMode.ONE -> AppColors.PrimaryGreen
            })
        }
    }
}