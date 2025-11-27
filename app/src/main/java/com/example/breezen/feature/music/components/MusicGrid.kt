package com.example.breezen.feature.music.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.breezen.core.network.IMAGE_BUCKET_URL
import com.example.breezen.core.network.Song
import com.example.breezen.core.ui.theme.DMSansFontFamily
import com.example.breezen.feature.music.TabViewModel
import com.example.breezen.feature.music.utils.playSongFromPlaylist
import kotlinx.coroutines.launch

@Composable
internal fun MusicItemsGrid(
    items: List<Song>,
    navController: NavController,
    viewModel: TabViewModel
) {
    val context = LocalContext.current

    // --- CACHED SEQUENTIAL LOADING ---
    val readyItems = remember { mutableStateListOf<Song>() }

    LaunchedEffect(items) {
        readyItems.clear()
        val loadedIds = mutableSetOf<String>()

        items.forEach { song ->
            launch {
                val imageUrl = IMAGE_BUCKET_URL + song.id + ".webp"
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .memoryCacheKey(imageUrl) // 1. Use Memory Cache Key
                    .listener(
                        onSuccess = { _, _ ->
                            if (!loadedIds.contains(song.id)) {
                                loadedIds.add(song.id)
                                readyItems.add(song)
                            }
                        }
                    )
                    .build()
                // 2. Pre-fetch into cache
                context.imageLoader.execute(request)
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = readyItems,
            key = { it.id }
        ) { musicItem ->
            // 3. Animate layout changes smoothly
            Box(modifier = Modifier.animateItem()) {
                MusicItemCard(
                    viewModel = viewModel,
                    musicItem = musicItem,
                    onClick = {
                        playSongFromPlaylist(
                            context = context,
                            viewModel = viewModel,
                            selectedSong = musicItem,
                            playlist = items,
                            navController = navController
                        )
                    }
                )
            }
        }
    }
}

@Composable
internal fun MusicItemCard(
    musicItem: Song,
    onClick: () -> Unit,
    viewModel: TabViewModel
) {
    var dominantColor by remember { mutableStateOf(Color(0xFF444444)) }
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Optimized Color Calculation
    val vinylLight by remember(dominantColor) { derivedStateOf { dominantColor.copy(alpha = 0.7f) } }
    val vinylDarker by remember(dominantColor) {
        derivedStateOf {
            dominantColor.copy(
                red = dominantColor.red * 0.7f,
                green = dominantColor.green * 0.7f,
                blue = dominantColor.blue * 0.7f
            )
        }
    }

    // Animation State
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    LaunchedEffect(musicItem.id) {
        try {
            val color = viewModel.getDominantColor(musicItem)
            dominantColor = color
            viewModel.songColorCache[musicItem.id] = color
        } catch (e: Exception) {
            Log.e("MusicItemCard", "Failed to load palette", e)
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(animationSpec = tween(400)) +
                scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                ) { onClick() }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                // VINYL BACKGROUND (Grooves + Color)
                Canvas(
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer() // 4. GPU Acceleration
                ) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.minDimension / 2
                    val labelRadius = radius * 0.48f

                    // Base Color
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(vinylLight, dominantColor, vinylDarker),
                            center = Offset(centerX, centerY),
                            radius = radius
                        ),
                        radius = radius
                    )

                    // Grooves
                    val outerGrooveStart = labelRadius + (radius * 0.05f)
                    val outerGrooveSpacing = (radius * 0.95f - outerGrooveStart) / 6f

                    for (i in 0 until 6) {
                        val grooveRadius = outerGrooveStart + (i * outerGrooveSpacing)
                        drawCircle(Color.Black.copy(alpha = 0.4f), radius = grooveRadius, style = Stroke(width = 2.2f))
                        drawCircle(Color.White.copy(alpha = 0.1f), radius = grooveRadius + 1.0f, style = Stroke(width = 1.4f))
                    }

                    // Shadow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(dominantColor.copy(alpha = 0.8f), Color.Transparent),
                            center = Offset(centerX, centerY),
                            radius = labelRadius
                        ),
                        radius = labelRadius
                    )
                }

                // Album Art
                SubcomposeAsyncImage(
                    model = IMAGE_BUCKET_URL + musicItem.id + ".webp",
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // OVERLAY (Hole only, NO DUST)
                Canvas(
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer() // GPU Acceleration
                ) {
                    val radius = size.minDimension / 2
                    val centerHoleRadius = radius * 0.12f

                    drawCircle(Color.Black, radius = centerHoleRadius)
                    drawCircle(Color.White.copy(alpha = 0.15f), radius = centerHoleRadius, style = Stroke(width = 1f))
                }
            }

            Text(
                text = musicItem.title,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = DMSansFontFamily,
                color = onBackgroundColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = musicItem.artist ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = DMSansFontFamily,
                color = onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}