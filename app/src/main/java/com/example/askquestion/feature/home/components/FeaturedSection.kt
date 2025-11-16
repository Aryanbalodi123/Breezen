package com.example.askquestion.feature.home.components

// --- THIS IS THE FIX ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.askquestion.R
import com.example.askquestion.core.network.Song
import com.example.askquestion.core.ui.components.ShimmerBox
import com.example.askquestion.feature.music.TabViewModel
import com.example.askquestion.feature.music.utils.playSongFromPlaylist

// --- END FIX ---

@Composable
fun FeaturedSection(
    navController: NavController,
    isLoading: Boolean,
    viewModel: TabViewModel,
    featuredSongs: List<Song> // Type is now core.network.Song
) {
    Column {
        Text(
            text = "Refreshing Tunes",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        )


        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            ,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FeatureSectionCard1(
                    song = featuredSongs.getOrNull(0),
                    viewModel = viewModel,
                    navController = navController,
                    isLoading = isLoading
                )
            }
            item {
                FeatureSectionCard2(
                    song = featuredSongs.getOrNull(1),
                    viewModel = viewModel,
                    navController = navController,
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun FeatureSectionCard1(
    song: Song?, // Type is now core.network.Song
    viewModel: TabViewModel,
    navController: NavController,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val allSongs by viewModel.allSongs

    Column(
        modifier = Modifier
            .height(350.dp)
            .width(250.dp)
            .clip(shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF012f46), Color(0xFF00090e), Color.Black),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.gradient_circles),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp)
                    )
                } else {
                    Text(
                        text = "${song?.duration?.div(60) ?: 0} min",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            )
                            .padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isLoading) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    )
                } else {
                    Text(
                        song?.title ?: "No Song",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 26.sp, fontWeight = FontWeight.Thin
                        ),
                    )
                }

                Spacer(Modifier.height(10.dp))

                IconButton(
                    onClick = {
                        if (!isLoading && song != null && allSongs.isNotEmpty()) {
                            playSongFromPlaylist(context, viewModel, song, allSongs, navController)
                        }
                    },
                    enabled = !isLoading && song != null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLoading) {
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF012f46).copy(alpha = 0.5f),
                                        Color(0xFF07a796).copy(alpha = 0.5f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(Color(0xFF012f46), Color(0xFF07a796))
                                )
                            }
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        modifier = Modifier.size(22.dp),
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureSectionCard2(
    song: Song?, // Type is now core.network.Song
    viewModel: TabViewModel,
    navController: NavController,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val allSongs by viewModel.allSongs

    Column(
        modifier = Modifier
            .height(350.dp)
            .width(250.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFdde46f),
                        Color(0xFF68a095),
                        Color(0xFF21366d),
                        Color(0xFF111333)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy((-50).dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentWidth(Alignment.End)
                    .offset(x = 16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer(rotationY = 45f)
                )

                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer(rotationY = 45f)
                )

                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(190.dp)
                        .graphicsLayer(rotationY = 45f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp)
                    )
                } else {
                    Text(
                        text = "${song?.duration?.div(60) ?: 0} min",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isLoading) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    )
                } else {
                    Text(
                        song?.title ?: "No Song",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Thin
                        ),
                    )
                }

                Spacer(Modifier.height(10.dp))

                IconButton(
                    onClick = {
                        if (!isLoading && song != null && allSongs.isNotEmpty()) {
                            playSongFromPlaylist(
                                context,
                                viewModel,
                                song,
                                allSongs,
                                navController
                            )
                        }
                    },
                    enabled = !isLoading && song != null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLoading) {
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFdde46f).copy(alpha = 0.5f),
                                        Color(0xFF68a095).copy(alpha = 0.5f),
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFdde46f),
                                        Color(0xFF68a095),
                                    )
                                )
                            }
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        modifier = Modifier.size(22.dp),
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}