package com.example.breezen.feature.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.CornerLarge
import com.example.breezen.core.ui.theme.CornerSmall
import com.example.breezen.core.ui.theme.CosmosBlackBlue
import com.example.breezen.core.ui.theme.DeepTeal
import com.example.breezen.core.ui.theme.IndigoNight
import com.example.breezen.core.ui.theme.MidnightBlue
import com.example.breezen.core.ui.theme.MistGreen
import com.example.breezen.core.ui.theme.OceanGreen
import com.example.breezen.core.ui.theme.SunriseYellow
import com.example.breezen.core.ui.theme.TextPrimary
import com.example.breezen.feature.meditation.MeditationViewModel
import com.example.breezen.feature.meditation.model.GuidedMeditation
import com.example.breezen.feature.meditation.model.createGuidedMeditationData

@Composable
fun FeaturedSection(
    navController: NavController,
    viewModel: MeditationViewModel
) {
    val meditation1 = remember { createGuidedMeditationData(viewModel, (0..7).random()) }
    val meditation2 = remember { createGuidedMeditationData(viewModel, (0..7).random()) }

    Column {
        Text(
            text = "Refreshing Tunes",
            style = AppTypography.headlineMedium.copy(color = TextPrimary),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FeatureSectionCard1(
                    meditation = meditation1,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            item {
                FeatureSectionCard2(
                    meditation = meditation2,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}

/* ---------------------------------------------
   Featured Card 1
--------------------------------------------- */

@Composable
fun FeatureSectionCard1(
    meditation: GuidedMeditation,
    viewModel: MeditationViewModel,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .height(350.dp)
            .width(250.dp)
            .clip(RoundedCornerShape(CornerLarge))
            .background(
                Brush.linearGradient(
                    colors = listOf(DeepTeal, MidnightBlue, AppBlack)
                )
            )
    ) {
        Box(Modifier.fillMaxSize()) {

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
                    style = AppTypography.bodySmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .background(
                            TextPrimary.copy(alpha = 0.20f),
                            RoundedCornerShape(CornerSmall)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Spacer(Modifier.weight(1f))

                AutoResizedSingleLineText(
                    text = meditation.title + " " + meditation.subtitle,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

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
                                colors = listOf(DeepTeal, OceanGreen)
                            )
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = null,
                        tint = AppBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/* ---------------------------------------------
   Featured Card 2
--------------------------------------------- */

@Composable
fun FeatureSectionCard2(
    meditation: GuidedMeditation,
    viewModel: MeditationViewModel,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .height(350.dp)
            .width(250.dp)
            .clip(RoundedCornerShape(CornerLarge))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        SunriseYellow,
                        MistGreen,
                        IndigoNight,
                        CosmosBlackBlue
                    )
                )
            )
    ) {
        Box(Modifier.fillMaxSize()) {

            Row(
                horizontalArrangement = Arrangement.spacedBy((-50).dp),
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentWidth(Alignment.End)
                    .offset(x = 16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer(rotationY = 45f)
                )
                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer(rotationY = 45f)
                )
                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
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
                Text(
                    text = "Trending",
                    style = AppTypography.bodySmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .background(
                            TextPrimary.copy(alpha = 0.20f),
                            RoundedCornerShape(CornerSmall)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Spacer(Modifier.weight(1f))

                AutoResizedSingleLineText(
                    text = meditation.title + " " + meditation.subtitle,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

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
                                listOf(DeepTeal, OceanGreen)
                            )
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = null,
                        tint = AppBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/* ---------------------------------------------
   Dynamic text sizing for titles
--------------------------------------------- */

@Composable
fun AutoResizedSingleLineText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 26.sp,
    minFontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    var currentSize by remember { mutableStateOf(maxFontSize) }
    val density = LocalDensity.current

    Box(modifier) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            color = color,
            fontWeight = fontWeight,
            fontSize = currentSize,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    val containerWidth = coords.size.width.toFloat()
                    val textPx = with(density) { currentSize.toPx() }
                    val estimatedText = textPx * text.length * 0.55f

                    if (estimatedText > containerWidth && currentSize > minFontSize) {
                        currentSize = (currentSize.value - 1).sp
                    }
                }
        )
    }
}
