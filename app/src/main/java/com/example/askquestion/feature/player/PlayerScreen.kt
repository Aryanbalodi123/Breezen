package com.example.askquestion.feature.player

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
import com.example.askquestion.core.network.IMAGE_BUCKET_URL
import com.example.askquestion.core.network.Song
import com.example.askquestion.core.ui.components.ShimmerBox
import com.example.askquestion.feature.music.PlayerLoadState
import com.example.askquestion.feature.music.PlayerUiState
import com.example.askquestion.feature.music.TabViewModel
import com.example.askquestion.feature.player.components.MusicPlayerControls
import com.example.askquestion.feature.player.components.MusicProgress
import com.example.askquestion.feature.player.components.NextUpCard
import com.example.askquestion.feature.player.components.PlayerInitialLoadScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull


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

    var isPlayerReady by remember { mutableStateOf(false) }
    var isImageLoaded by remember { mutableStateOf(false) }
    var currentTime by remember { mutableLongStateOf(0L) }

    val player = remember { ExoPlayer.Builder(context).build() }

    val dominantColor by animateColorAsState(
        targetValue = uiState.dominantColor,
        animationSpec = tween(500),
        label = "dominantColor"
    )

    LaunchedEffect(uiState.streamUrl) {
        if (uiState.streamUrl.isNotEmpty()) {
            isPlayerReady = false

            Log.d("PlayerScreen", "⏯ Setting up player for ${uiState.streamUrl}")

            try {
                player.stop()
                player.clearMediaItems()

                val item = MediaItem.Builder()
                    .setUri(uiState.streamUrl)
                    .setMediaId(uiState.streamUrl)
                    .build()

                player.setMediaItem(item)
                player.prepare()
                player.playWhenReady = true
            } catch (e: Exception) {
                Log.e("PlayerScreen", "Player prepare failed", e)
            }
        }
    }

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

                if (state == Player.STATE_ENDED) {
                    Log.d("PlayerScreen", "Song ended, playing next")
                    viewModel.playNextSong(context, forceManual = false)
                }
            }
        }

        player.addListener(listener)

        try {
            while (isActive) {
                try {
                    currentTime = player.currentPosition
                } catch (_: Exception) {
                }
                delay(500)
            }
        } finally {
            player.removeListener(listener)
        }
    }

    LaunchedEffect(currentSong?.id) {
        val id = currentSong?.id ?: return@LaunchedEffect
        isImageLoaded = false

        val success = withTimeoutOrNull(2000) {
            val request = ImageRequest.Builder(context)
                .data(IMAGE_BUCKET_URL + id + ".webp")
                .build()

            context.imageLoader.execute(request)
            true
        }

        isImageLoaded = true

        Log.d(
            "PlayerScreen",
            if (success == true) "✔ Image Loaded" else "⚠ Timeout Image"
        )
    }

    LaunchedEffect(isPlayerReady, isImageLoaded) {
        if (isPlayerReady && isImageLoaded) {
            viewModel.onPlayerReadyAndImageLoaded()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PlayerContent(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
            hazeState = hazeState,
            uiState = uiState,
            isImageLoaded = isImageLoaded,
            dominantColor = dominantColor,
            currentTime = currentTime,
            isPlaying = viewModel.isPlaying,
            isShuffleEnabled = viewModel.isShuffleEnabled,
            repeatMode = viewModel.repeatMode,
            onSeek = { t ->
                try {
                    player.seekTo(t)
                } catch (_: Exception) {
                }
            },
            onEvent = { event ->
                when (event) {
                    PlayerEvent.PlayPause ->
                        if (player.isPlaying) player.pause() else player.play()

                    PlayerEvent.Next -> {
                        player.stop()
                        viewModel.playNextSong(context, forceManual = true)
                    }

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

        AnimatedVisibility(
            visible = uiState.loadState == PlayerLoadState.INITIAL,
            enter = fadeIn(),
            exit = fadeOut(tween(500))
        ) {
            PlayerInitialLoadScreen()
        }
    }
}

@Composable
internal fun PlayerContent(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    uiState: PlayerUiState,
    isImageLoaded: Boolean,
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
    // This is an artistic choice, not a theme color. It stays.
    val beigeColor = Color(0xFFF5F5DC)

    val showShimmer = !isImageLoaded
    val effectiveCurrentTime = if (showShimmer) 0L else currentTime

    val songDurationMs =
        if ((currentSong?.duration ?: 0) > 0) currentSong!!.duration * 1000L else 1000L
    val (minuteTotal, secondTotal) = minSec(songDurationMs)
    val (minuteCurrent, secondCurrent) = minSec(effectiveCurrentTime)

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
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
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Progress + Image
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                MusicProgress(
                    currentTime = effectiveCurrentTime,
                    duration = songDurationMs,
                    strokeColor = beigeColor, // Artistic choice
                    onSeek = onSeek
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                            append(String.format("%02d", minuteCurrent))
                            append(":")
                            append(String.format("%02d", secondCurrent))
                        }
                        append(" | ")
                        withStyle(SpanStyle(color = beigeColor)) { // Artistic choice
                            append(String.format("%02d", minuteTotal))
                            append(":")
                            append(String.format("%02d", secondTotal))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
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
                        ShimmerBox(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
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

            Spacer(modifier = Modifier.height(32.dp))
            NextUpCard(hazeState = hazeState, nextUpSong = uiState.nextUpSong as Song?)

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// This is an artistic gradient based on the song color, so it stays.
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