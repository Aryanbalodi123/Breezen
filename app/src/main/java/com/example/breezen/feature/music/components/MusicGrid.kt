// FULL FILE WITH WIDER GROOVES
package com.example.breezen.feature.music.components

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.breezen.core.network.IMAGE_BUCKET_URL
import com.example.breezen.core.network.Song
import com.example.breezen.core.ui.theme.DMSansFontFamily
import com.example.breezen.feature.music.TabViewModel
import com.example.breezen.feature.music.utils.playSongFromPlaylist
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun MusicItemsGrid(
    items: List<Song>,
    navController: NavController,
    viewModel: TabViewModel
) {
    val context = LocalContext.current

    var loadedItems by remember { mutableStateOf(setOf<Int>()) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(items) { index, musicItem ->
            MusicItemCard(
                viewModel = viewModel,
                musicItem = musicItem,
                isLoaded = index in loadedItems,
                onImageLoaded = {
                    loadedItems = loadedItems + index
                },
                onClick = {
                    playSongFromPlaylist(
                        context = context,
                        viewModel = viewModel,
                        selectedSong = musicItem,
                        playlist = items,
                        navController = navController
                    )
                })
        }
    }
}

@Composable
internal fun MusicItemCard(
    musicItem: Song,
    isLoaded: Boolean,
    onImageLoaded: (() -> Unit)? = null,
    onClick: () -> Unit,
    viewModel: TabViewModel
) {
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color(0xFF444444)) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val scale by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ), label = "popScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f, animationSpec = tween(300), label = "popAlpha"
    )

    LaunchedEffect(musicItem.id) {
        try {
            dominantColor = viewModel.getDominantColor(musicItem)
        } catch (e: Exception) {
            Log.e("MusicItemCard", "Failed to load palette", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = ripple()
            ) { if (isLoaded) onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Box(
            modifier = Modifier
                .size(220.dp)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(220.dp)) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension / 2

                val vinylLight = dominantColor.copy(alpha = 0.7f)
                val vinylDarker = dominantColor.copy(
                    red = dominantColor.red * 0.7f,
                    green = dominantColor.green * 0.7f,
                    blue = dominantColor.blue * 0.7f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            vinylLight,
                            dominantColor,
                            vinylDarker
                        ),
                        center = Offset(centerX, centerY),
                        radius = radius
                    ),
                    radius = radius
                )

                val labelRadius = radius * 0.48f

                // OUTER GROOVES — WIDER NOW
                val outerGrooveStart = labelRadius + (radius * 0.05f)
                val outerGrooveEnd = radius * 0.95f
                val outerGrooveCount = 6
                val outerGrooveSpacing = (outerGrooveEnd - outerGrooveStart) / outerGrooveCount.toFloat()

                for (i in 0 until outerGrooveCount) {
                    val grooveRadius = outerGrooveStart + (i * outerGrooveSpacing)

                    drawCircle(
                        color = Color.Black.copy(alpha = 0.4f),
                        radius = grooveRadius,
                        style = Stroke(width = 2.2f)   // widened
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = grooveRadius + 1.0f,
                        style = Stroke(width = 1.4f)   // widened
                    )
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.8f),
                            dominantColor.copy(alpha = 0.6f),
                            dominantColor.copy(alpha = 0.4f)
                        ),
                        center = Offset(centerX, centerY),
                        radius = labelRadius
                    ),
                    radius = labelRadius
                )

                for (i in 0..3) {
                    val ringRadius = labelRadius - (i * 2.5f)
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.15f),
                        radius = ringRadius,
                        style = Stroke(width = 0.8f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = ringRadius + 0.5f,
                        style = Stroke(width = 0.5f)
                    )
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.9f),
                            dominantColor.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = radius * 0.35f
                    ),
                    radius = radius * 0.35f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f)
                        ),
                        center = Offset(centerX, centerY),
                        radius = radius
                    ),
                    radius = radius,
                    style = Stroke(width = 3f)
                )
            }

            SubcomposeAsyncImage(
                model = IMAGE_BUCKET_URL + musicItem.id + ".webp",
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onSuccess = { onImageLoaded?.invoke() },
                onError = { onImageLoaded?.invoke() }
            )

            // NEW CANVAS WITH INNER GROOVES (WIDER)
            Canvas(modifier = Modifier.size(220.dp)) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension / 2

                val labelRadius = radius * 0.48f
                val centerHoleRadius = radius * 0.12f

                val innerGrooveStart = centerHoleRadius + (radius * 0.06f)
                val innerGrooveEnd = labelRadius - (radius * 0.06f)

                // INNER GROOVE 1 — WIDER
                val groove1Radius = innerGrooveStart + (innerGrooveEnd - innerGrooveStart) * 0.2f
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = groove1Radius,
                    style = Stroke(width = 2f)  // widened
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = groove1Radius + 0.8f,
                    style = Stroke(width = 1.2f)  // widened
                )

                // INNER GROOVE 2 — WIDER
                val groove2Radius = innerGrooveStart + (innerGrooveEnd - innerGrooveStart) * 0.7f
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = groove2Radius,
                    style = Stroke(width = 2f) // widened
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = groove2Radius + 0.8f,
                    style = Stroke(width = 1.2f) // widened
                )

                // Hole, dust, etc (unchanged)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Black.copy(alpha = 0.8f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = centerHoleRadius * 1.5f
                    ),
                    radius = centerHoleRadius * 1.5f
                )

                drawCircle(
                    color = Color.Black,
                    radius = centerHoleRadius
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = centerHoleRadius,
                    style = Stroke(width = 1f)
                )

                val random = java.util.Random(musicItem.id.hashCode().toLong())
                for (i in 0..8) {
                    val dustAngle = random.nextFloat() * 360f
                    val dustDistance = (radius * 0.4f) + (random.nextFloat() * radius * 0.5f)
                    val dustX = centerX + dustDistance * cos(Math.toRadians(dustAngle.toDouble())).toFloat()
                    val dustY = centerY + dustDistance * sin(Math.toRadians(dustAngle.toDouble())).toFloat()

                    drawCircle(
                        color = Color.White.copy(alpha = 0.02f),
                        radius = 1f + random.nextFloat() * 2f,
                        center = Offset(dustX, dustY)
                    )
                }
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
            text = musicItem.artist ?: "Unknown Artist",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = DMSansFontFamily,
            color = onSurfaceColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
