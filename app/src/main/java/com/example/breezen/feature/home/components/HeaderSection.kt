package com.example.breezen.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.breezen.R
// --- FIX: Corrected import ---
import com.example.breezen.core.network.Song
// --- END FIX ---
import com.example.breezen.core.ui.components.ShimmerBox
import com.example.breezen.feature.music.TabViewModel
import com.example.breezen.feature.music.utils.playSongFromPlaylist
import com.example.breezen.core.ui.util.gradientBackground

@Composable
fun HeaderSection(
    song: Song?,
    viewModel: TabViewModel,
    navController: NavController,
    isLoading: Boolean,
    username: String
) {
    val context = LocalContext.current
    val allSongs by viewModel.allSongs

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(shape = RoundedCornerShape(bottomEnd = 120.dp))
    ) {
        Row {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .gradientBackground(
                            listOf(
                                Color.Black, Color.Black, Color.Black,
                                Color(0xFF294577), Color(0xFF91658f), Color(0xFFc8b2c7)
                            ), angle = 45f
                        )
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "GOOD MORNING ${username.uppercase()}",
                style = MaterialTheme.typography.bodySmall.copy(
                    letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (isLoading) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp)
                )
            } else {
                Text(
                    text = song?.title ?: "No Song",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Light, letterSpacing = 2.sp, fontSize = 48.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (isLoading) {
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                )
            } else {
                Text(
                    text = "${song?.duration?.div(60) ?: 0} MINUTES",
                    style = MaterialTheme.typography.bodySmall.copy(
                        letterSpacing = 2.sp, fontWeight = FontWeight.Bold, fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(24.dp))
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
                        if (isLoading) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.onBackground
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