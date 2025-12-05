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
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.SolidBlack
import com.example.breezen.core.ui.theme.TextPrimary
import com.example.breezen.core.ui.theme.TextSecondary
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

    // Sequential preloading of images
    val readyItems = remember { mutableStateListOf<Song>() }

    LaunchedEffect(items) {
        readyItems.clear()
        val loaded = mutableSetOf<String>()

        items.forEach { song ->
            launch {
                val url = IMAGE_BUCKET_URL + song.id + ".webp"
                val req = ImageRequest.Builder(context)
                    .data(url)
                    .memoryCacheKey(url)
                    .listener(onSuccess = { _, _ ->
                        if (loaded.add(song.id)) readyItems.add(song)
                    })
                    .build()

                context.imageLoader.execute(req)
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = readyItems,
            key = { it.id }
        ) { item ->
            Box(modifier = Modifier.animateItem()) {
                MusicItemCard(
                    musicItem = item,
                    viewModel = viewModel,
                    onClick = {
                        playSongFromPlaylist(
                            context = context,
                            viewModel = viewModel,
                            selectedSong = item,
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
    var dominant by remember { mutableStateOf(SolidBlack.copy(alpha = 0.6f)) }

    // Derived palette shades
    val vinylLight by remember(dominant) { derivedStateOf { dominant.copy(alpha = 0.7f) } }
    val vinylDark by remember(dominant) {
        derivedStateOf {
            dominant.copy(
                red = dominant.red * 0.7f,
                green = dominant.green * 0.7f,
                blue = dominant.blue * 0.7f
            )
        }
    }

    // Entry animation
    val visible = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    // Extract color palette
    LaunchedEffect(musicItem.id) {
        try {
            val c = viewModel.getDominantColor(musicItem)
            dominant = c
            viewModel.songColorCache[musicItem.id] = c
        } catch (e: Exception) {
            Log.e("MusicItemCard", "Palette error", e)
        }
    }

    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(400)) +
                scaleIn(initialScale = 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                ) { onClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(SolidBlack),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl base + grooves
                Canvas(
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer()
                ) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    val labelRadius = radius * 0.48f

                    // Color blend
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(vinylLight, dominant, vinylDark),
                            center = center,
                            radius = radius
                        ),
                        radius = radius
                    )

                    // Grooves
                    val start = labelRadius + (radius * 0.05f)
                    val spacing = (radius * 0.95f - start) / 6f

                    repeat(6) { i ->
                        val r = start + (i * spacing)
                        drawCircle(
                            SolidBlack.copy(alpha = 0.4f),
                            radius = r,
                            style = Stroke(width = 2.2f)
                        )
                        drawCircle(
                            AppWhite.copy(alpha = 0.1f),
                            radius = r + 1f,
                            style = Stroke(width = 1.4f)
                        )
                    }

                    // Shadow on center label
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(dominant.copy(alpha = 0.8f), Color.Transparent),
                            center = center,
                            radius = labelRadius
                        ),
                        radius = labelRadius
                    )
                }

                // Cover Art
                SubcomposeAsyncImage(
                    model = IMAGE_BUCKET_URL + musicItem.id + ".webp",
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Vinyl hole
                Canvas(
                    modifier = Modifier.size(220.dp)
                ) {
                    val radius = size.minDimension / 2
                    val hole = radius * 0.12f

                    drawCircle(SolidBlack, radius = hole)
                    drawCircle(AppWhite.copy(alpha = 0.15f),
                        radius = hole,
                        style = Stroke(width = 1f))
                }
            }

            // Song Title
            Text(
                text = musicItem.title,
                style = AppTypography.bodyMedium.copy(color = TextPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // Artist
            Text(
                text = musicItem.artist,
                style = AppTypography.bodySmall.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
