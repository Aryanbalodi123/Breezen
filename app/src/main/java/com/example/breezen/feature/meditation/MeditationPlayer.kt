package com.example.breezen.feature.meditation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.example.breezen.R
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.TextPrimary
import com.example.breezen.core.ui.theme.WhiteAlpha20
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MeditationPlayer(
    navController: NavController,
    viewModel: MeditationViewModel
) {
    val context = LocalContext.current

    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(1L) }

    // Initialize and release ExoPlayer with this composable's lifecycle
    DisposableEffect(Unit) {
        val exo = ExoPlayer.Builder(context).build().apply {
            val item = MediaItem.fromUri(viewModel.currentSongUrl)
            setMediaItem(item)
            prepare()
            playWhenReady = true
        }
        player = exo

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        totalDuration = exo.duration.coerceAtLeast(1L)
                        isLoading = false
                    }

                    Player.STATE_BUFFERING -> isLoading = true
                    Player.STATE_ENDED,
                    Player.STATE_IDLE -> isLoading = false
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }

        exo.addListener(listener)

        onDispose {
            exo.removeListener(listener)
            exo.release()
            player = null
        }
    }

    // Restart playback when the current song URL changes
    LaunchedEffect(viewModel.currentSongUrl) {
        player?.apply {
            setMediaItem(MediaItem.fromUri(viewModel.currentSongUrl))
            prepare()
            playWhenReady = true
        }
    }

    // Poll player position while this composable is active to update progress
    LaunchedEffect(Unit) {
        while (isActive) {
            player?.let { p ->
                currentPosition = p.currentPosition
                if (p.duration > 0) {
                    totalDuration = p.duration
                }
                isPlaying = p.isPlaying
            }
            delay(50)
        }
    }

    val progress = if (totalDuration > 0) {
        (currentPosition.toFloat() / totalDuration).coerceIn(0f, 1f)
    } else {
        0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBlack)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MeditationTopCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(4.dp),
                title = viewModel.passedTitle,
                subtitle = viewModel.passedSubTitle,
                vectorRes = viewModel.passedVectorRes,
                currentPosition = currentPosition,
                totalDuration = totalDuration,
                viewModel = viewModel
            )

            MeditationControlButtons(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                viewModel = viewModel,
                isPlaying = isPlaying,
                isLoading = isLoading,
                onPlayPause = {
                    player?.let { p ->
                        if (p.isPlaying) p.pause() else p.play()
                    }
                }
            )

            MeditationRuler(
                progress = progress,
                tickCount = 50,
                viewModel = viewModel,
                totalDuration = totalDuration,
                onSeek = { fraction ->
                    player?.let { p ->
                        if (p.duration > 0) {
                            val seekPos = (p.duration * fraction).toLong().coerceIn(0, p.duration)
                            p.seekTo(seekPos)
                        }
                    }
                }
            )

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun MeditationTopCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    vectorRes: Int,
    currentPosition: Long,
    totalDuration: Long,
    viewModel: MeditationViewModel
) {
    Column(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(viewModel.GlassGradient)
            .border(1.dp, viewModel.GlassBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 40.dp.value.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FunnelDisplayFamily,
            color = TextPrimary
        )

        Text(
            text = subtitle,
            fontSize = 40.dp.value.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FunnelDisplayFamily,
            color = TextPrimary
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(vectorRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(viewModel.passedColor)
            )
        }

        Row {
            Text(
                text = viewModel.formatTime(currentPosition),
                style = AppTypography.headlineLarge.copy(
                    fontFamily = FunnelDisplayFamily,
                    color = TextPrimary.copy(alpha = 0.5f)
                )
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = viewModel.formatTime(totalDuration),
                style = AppTypography.headlineLarge.copy(
                    fontFamily = FunnelDisplayFamily,
                    color = TextPrimary
                )
            )
        }
    }
}

@Composable
fun MeditationControlButtons(
    modifier: Modifier,
    viewModel: MeditationViewModel,
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPause: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isLoading) {
            LoadingButton(viewModel)
        } else {
            ControlButton(
                iconRes = if (isPlaying) R.drawable.pause else R.drawable.play,
                viewModel = viewModel,
                enabled = true
            ) {
                onPlayPause()
            }
        }

        ControlButton(
            iconRes = R.drawable.music_previous,
            viewModel = viewModel,
            enabled = !isLoading
        ) {
            viewModel.skipToPrevious()
        }

        ControlButton(
            iconRes = R.drawable.music_next,
            viewModel = viewModel,
            enabled = !isLoading
        ) {
            viewModel.skipToNext()
        }
    }
}

@Composable
fun RowScope.ControlButton(
    iconRes: Int,
    viewModel: MeditationViewModel,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.GlassGradient)
            .border(1.dp, viewModel.GlassBorder, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (enabled) TextPrimary else TextPrimary.copy(alpha = 0.3f),
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
fun RowScope.LoadingButton(
    viewModel: MeditationViewModel
) {
    var rotation by remember { mutableFloatStateOf(0f) }

    // Simple rotation loop used for the loading indicator
    LaunchedEffect(Unit) {
        while (isActive) {
            rotation += 10f
            if (rotation >= 360f) rotation = 0f
            delay(16)
        }
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.GlassGradient)
            .border(1.dp, viewModel.GlassBorder, RoundedCornerShape(20.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(40.dp)
        ) {
            val strokeWidth = 4.dp.toPx()

            drawArc(
                color = WhiteAlpha20,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(strokeWidth)
            )

            drawArc(
                color = TextPrimary,
                startAngle = rotation,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
fun MeditationRuler(
    progress: Float,
    tickCount: Int,
    viewModel: MeditationViewModel,
    totalDuration: Long,
    onSeek: (Float) -> Unit
) {
    val density = LocalDensity.current
    var widthPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .onGloballyPositioned { widthPx = it.size.width.toFloat() }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (widthPx > 0 && totalDuration > 0) {
                        onSeek((offset.x / widthPx).coerceIn(0f, 1f))
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    if (widthPx > 0 && totalDuration > 0) {
                        onSeek((change.position.x / widthPx).coerceIn(0f, 1f))
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(tickCount) { i ->
                val tickFraction =
                    if (tickCount > 1) i / (tickCount - 1).toFloat() else 0f
                val passed = tickFraction <= progress
                val color = if (passed) {
                    viewModel.passedColor.copy(alpha = 0.9f)
                } else {
                    WhiteAlpha20
                }
                val height = if (i % 5 == 0) 30.dp else 15.dp

                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(height)
                        .background(color, RoundedCornerShape(50))
                )
            }
        }

        if (widthPx > 0) {
            val cursorOffset = widthPx * progress
            val cursorDp = with(density) { cursorOffset.toDp() }

            Box(
                modifier = Modifier
                    .offset(x = cursorDp - 2.dp)
                    .width(4.dp)
                    .height(50.dp)
                    .background(viewModel.passedColor, RoundedCornerShape(16.dp))
            )
        }
    }
}
