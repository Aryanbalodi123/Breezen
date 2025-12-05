package com.example.breezen.feature.home.components

// --- FIX: Corrected import ---
// --- END FIX ---
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
import com.example.breezen.core.network.Song
import com.example.breezen.core.ui.components.ShimmerBox
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.TextPrimary
import com.example.breezen.core.ui.util.gradientBackground
import com.example.breezen.feature.music.TabViewModel
import com.example.breezen.feature.music.utils.playSongFromPlaylist

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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "GOOD MORNING ${username.uppercase()}",
                style = AppTypography.bodySmall.copy(
                    letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
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
                    style = AppTypography.displayMedium.copy(
                        fontWeight = FontWeight.Light, letterSpacing = 2.sp, fontSize = 48.sp
                    ),
                    color = TextPrimary
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
                    style = AppTypography.bodySmall.copy(
                        letterSpacing = 2.sp, fontWeight = FontWeight.Bold, fontSize = 12.sp
                    ),
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(36.dp))
            IconButton(
                onClick = {
                    if (!isLoading && song != null && allSongs.isNotEmpty()) {
                        playSongFromPlaylist(context, viewModel, song, allSongs, navController)
                    }
                },
                enabled = !isLoading && song != null,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        AppWhite
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    modifier = Modifier.size(28.dp),
                    contentDescription = "Play",
                    tint = AppBlack
                )
            }
        }
    }
}