package com.example.breezen.feature.meditation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.breezen.R
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.Prata
import com.example.breezen.core.ui.theme.pastelColors
import com.example.breezen.feature.meditation.components.CARD_WIDTH
import com.example.breezen.feature.meditation.components.GuidedMeditationCardWithEffect
import com.example.breezen.feature.meditation.model.GuidedMeditation
import com.example.breezen.feature.meditation.model.pastelShade

private val CARD_SPACING = (-80).dp

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MeditationGuidedScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MeditationViewModel
) {
    // ... (Vector Drawables and Color Logic remain the same) ...
    val vectorDrawables = remember {
        listOf(
            R.drawable.pebble, R.drawable.ring, R.drawable.tablet_broken,
            R.drawable.v_component, R.drawable.three_d, R.drawable.triangle,
            R.drawable.three_circle
        )
    }

    val shuffledColors = remember {
        val colors = pastelColors.shuffled().toMutableList()
        for (i in 1 until colors.size) {
            if (colors[i] == colors[i - 1]) {
                colors[i] = pastelColors.first { it != colors[i - 1] }
            }
        }
        colors
    }

    val guidedMeditations = remember {
        List(8) { index ->
            val vector = vectorDrawables[index % 7]
            val baseColor = shuffledColors[index % pastelColors.size]
            val shade = pastelShade(baseColor, 0.55f)
            val parts = viewModel.mp3ToTitle[index][1].split(" - ")

            GuidedMeditation(
                title = parts.firstOrNull() ?: "",
                subtitle = parts.getOrNull(1) ?: "",
                backgroundColor = baseColor,
                titleColor = Color.Black,
                secondaryColor = shade,
                vectorResId = vector,
                currentIndex = index
            )
        }
    }

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState)

    // Calculate padding so the first card is centered
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val contentPadding = (screenWidth - CARD_WIDTH) / 2

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF070707), Color(0xFF000000))))
            .padding(top = 48.dp)
    ) {
        // ... (Header Text Section remains the same) ...
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Explore", fontFamily = FunnelDisplayFamily, fontSize = 54.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Peace", fontFamily = FunnelDisplayFamily, fontSize = 54.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
            Text("Through", fontFamily = Prata, fontSize = 48.sp, color = Color.LightGray.copy(alpha = 0.5f))
            Text("Practice", fontFamily = Prata, fontSize = 48.sp, color = Color.LightGray.copy(alpha = 0.5f))
        }

        Spacer(modifier = Modifier.weight(1f))

        // List
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp),
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = contentPadding),
            horizontalArrangement = Arrangement.spacedBy(CARD_SPACING)
        ) {
            itemsIndexed(guidedMeditations) { index, meditation ->
                // Using the imported component
                GuidedMeditationCardWithEffect(
                    meditation = meditation,
                    listState = listState,
                    index = index,
                    contentPaddingPx = with(LocalDensity.current) { contentPadding.toPx() },
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}