package com.example.breezen.feature.meditation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.breezen.core.ui.theme.Prata
import com.example.breezen.feature.meditation.MeditationViewModel
import com.example.breezen.feature.meditation.model.GuidedMeditation
import kotlin.math.abs
import kotlin.math.max

val CARD_WIDTH = 350.dp
private const val MAX_TILT_DEGREES = 20f
private const val MAX_SCALE_REDUCTION = 0.6f
private const val MAX_TRANSLATE_X = 30f
private const val MIN_SIDE_ALPHA = 0.5f
private const val MAX_ELEVATION = 10f

@Composable
fun GuidedMeditationCardWithEffect(
    meditation: GuidedMeditation,
    listState: LazyListState,
    index: Int,
    contentPaddingPx: Float,
    viewModel: MeditationViewModel,
    navController: NavController
) {
    val density = LocalDensity.current
    val cardWidthPx = with(density) { CARD_WIDTH.toPx() }
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    val scrollOffsetPx = remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                ?.let { info ->
                    val itemCenter = info.offset + info.size / 2f
                    val viewportCenter = screenWidthPx / 2f
                    itemCenter - viewportCenter
                } ?: 0f
        }
    }.value

    val maxOffsetPx = contentPaddingPx + cardWidthPx / 2f
    val closeness = 1f - max(0f, abs(scrollOffsetPx) / maxOffsetPx)

    val scale = 1f - (1f - MAX_SCALE_REDUCTION) * (1f - closeness)
    val elevation = MAX_ELEVATION * closeness
    val rotationY = if (scrollOffsetPx < 0)
        MAX_TILT_DEGREES * (1f - closeness)
    else
        -MAX_TILT_DEGREES * (1f - closeness)

    val translateXCurve = (scrollOffsetPx / maxOffsetPx) * MAX_TRANSLATE_X
    val gapCorrection = abs(scrollOffsetPx) * (1f - closeness) * 0.4f
    val finalTranslationX =
        if (scrollOffsetPx < 0) translateXCurve + gapCorrection
        else translateXCurve - gapCorrection

    val centerPulse by animateFloatAsState(
        targetValue = if (abs(scrollOffsetPx) < cardWidthPx * 0.1f) 1.05f else 1f,
        animationSpec = tween(600)
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (abs(scrollOffsetPx) < cardWidthPx * 0.15f) 1f else 0f,
        animationSpec = tween(500)
    )

    val textTranslate by animateFloatAsState(
        targetValue = if (abs(scrollOffsetPx) < cardWidthPx * 0.15f) 0f else 40f,
        animationSpec = tween(500)
    )

    Card(
        modifier = Modifier
            .width(CARD_WIDTH)
            .height(400.dp)
            .zIndex(closeness * 10f)
            .graphicsLayer {
                cameraDistance = 8000f * density.density
                scaleX = scale * centerPulse
                scaleY = scale * centerPulse
                this.rotationY = rotationY
                translationX = finalTranslationX
                shadowElevation = elevation
                alpha = MIN_SIDE_ALPHA + (1f - MIN_SIDE_ALPHA) * closeness
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = meditation.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp)
        ) {
            Text(
                meditation.title,
                fontFamily = Prata,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha
                    translationY = textTranslate
                }
            )

            Text(
                meditation.subtitle,
                fontFamily = Prata,
                fontSize = 26.sp,
                fontWeight = FontWeight.Normal,
                color = meditation.secondaryColor.copy(alpha = 0.7f),
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha
                    translationY = textTranslate + 8
                }
            )

            Box(Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(id = meditation.vectorResId),
                    contentDescription = null,
                    tint = meditation.secondaryColor.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            translationY = -20 * closeness
                        }
                )
            }
        }
    }
}
