package com.example.breezen.feature.home.components

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.breezen.R
import com.example.breezen.feature.meditation.MeditationViewModel
import com.example.breezen.feature.meditation.model.GuidedMeditation
import createGuidedMeditationData

// --- END FIX ---

@Composable
fun FeaturedSection(
    navController: NavController,
    isLoading: Boolean,
    viewModel: MeditationViewModel,
) {

    // Generate two different meditations immediately
    val meditation1 = remember { createGuidedMeditationData(viewModel, (0..7).random()) }
    val meditation2 = remember { createGuidedMeditationData(viewModel, (0..7).random()) }

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
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FeatureSectionCard1(
                    meditation = meditation1,
                    viewModel = viewModel,
                    navController = navController,
                    isLoading = isLoading
                )
            }
            item {
                FeatureSectionCard2(
                    meditation = meditation2,
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
    meditation: GuidedMeditation,
    viewModel: MeditationViewModel,
    navController: NavController,
    isLoading: Boolean
) {

    Column(
        modifier = Modifier
            .height(350.dp)
            .width(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF012f46), Color(0xFF00090e), Color.Black)
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

                Text(
                    text = "New",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(10.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Two-line title FIX
                AutoResizedSingleLineText(
                    text = meditation.title + " " + meditation.subtitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxFontSize = 26.sp,
                    minFontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                IconButton(
                    onClick = {
                        viewModel.setAttributes(
                            meditation.backgroundColor,
                            meditation.title,
                            meditation.subtitle,
                            meditation.vectorResId,
                            meditation.currentIndex,
                            viewModel.mp3ToTitle[meditation.currentIndex][0]
                        )
                        navController.navigate("guided_meditate_player")
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF012f46), Color(0xFF07a796))
                            )
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureSectionCard2(
    meditation: GuidedMeditation,
    viewModel: MeditationViewModel,
    navController: NavController,
    isLoading: Boolean
) {

    Column(
        modifier = Modifier
            .height(350.dp)
            .width(250.dp)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFdde46f),
                        Color(0xFF68a095),
                        Color(0xFF21366d),
                        Color(0xFF111333)
                    )
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
                    modifier = Modifier.size(100.dp).graphicsLayer(rotationY = 45f)
                )
                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp).graphicsLayer(rotationY = 45f)
                )
                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    modifier = Modifier.size(190.dp).graphicsLayer(rotationY = 45f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Text(
                    text = "Trending",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(10.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Two-line title fix
                AutoResizedSingleLineText(
                    text = meditation.title + " " + meditation.subtitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxFontSize = 26.sp,
                    minFontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                IconButton(
                    onClick = {
                        viewModel.setAttributes(
                            meditation.backgroundColor,
                            meditation.title,
                            meditation.subtitle,
                            meditation.vectorResId,
                            meditation.currentIndex,
                            viewModel.mp3ToTitle[meditation.currentIndex][0]
                        )
                        navController.navigate("guided_meditate_player")
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF012f46), Color(0xFF07a796))
                            )
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun AutoResizedSingleLineText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 26.sp,
    minFontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Thin
) {
    var currentTextSize by remember { mutableStateOf(maxFontSize) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        Text(
            text = text,
            color = color,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            fontSize = currentTextSize,
            fontWeight = fontWeight,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->

                    val containerWidth = coords.size.width.toFloat()

                    // Convert sp -> px using density
                    val fontPx = with(density) { currentTextSize.toPx() }

                    // Approx width of characters
                    val textWidth = fontPx * text.length * 0.55f

                    // Shrink if needed
                    if (textWidth > containerWidth && currentTextSize.value > minFontSize.value) {
                        currentTextSize = (currentTextSize.value - 1).sp
                    }
                }
        )
    }
}

