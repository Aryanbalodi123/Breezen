package com.example.breezen.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import kotlinx.coroutines.delay

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
    var startAnimation by remember { mutableStateOf(false) }

    // Ensures we show shimmer if loading OR if song hasn't arrived yet (is null)
    val isContentReady = !isLoading && song != null

    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(shape = RoundedCornerShape(bottomEnd = 120.dp))
    ) {
        Row {
            repeat(5) { index ->
                val stripAlpha by animateFloatAsState(
                    targetValue = if (startAnimation) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = 1500, // Slower fade in
                        delayMillis = index * 150, // Slower staggered effect
                        easing = FastOutSlowInEasing
                    ),
                    label = "stripAlpha"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .alpha(stripAlpha)
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
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1500)) + slideInVertically(
                    initialOffsetY = { -60 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessVeryLow
                    )
                )
            ) {
                Text(
                    "GOOD MORNING ${username.uppercase()}",
                    style = AppTypography.bodySmall.copy(
                        letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
            }

            Crossfade(
                targetState = isContentReady,
                animationSpec = tween(durationMillis = 1000), // Slower crossfade
                label = "TitleCrossfade"
            ) { ready ->
                if (!ready) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(48.dp)
                    )
                } else {
                    // Song is guaranteed non-null here due to isContentReady check
                    Text(
                        text = song!!.title,
                        style = AppTypography.displayMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp,
                            fontSize = 48.sp
                        ),
                        color = TextPrimary
                    )
                }
            }

            Crossfade(
                targetState = isContentReady,
                animationSpec = tween(durationMillis = 1000),
                label = "TimeCrossfade"
            ) { ready ->
                if (!ready) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp)
                    )
                } else {
                    Text(
                        text = "${song!!.duration.div(60)} MINUTES",
                        style = AppTypography.bodySmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            AnimatedVisibility(
                visible = isContentReady,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessVeryLow // Very slow, gentle spring
                    )
                ) + fadeIn(tween(1500, delayMillis = 500))
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "buttonScale"
                )

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(AppWhite)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (isContentReady && allSongs.isNotEmpty()) {
                                playSongFromPlaylist(
                                    context,
                                    viewModel,
                                    song!!,
                                    allSongs,
                                    navController
                                )
                            }
                        },
                    contentAlignment = androidx.compose.ui.Alignment.Center
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
}