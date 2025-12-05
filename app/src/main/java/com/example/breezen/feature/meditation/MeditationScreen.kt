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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.Prata
import com.example.breezen.feature.meditation.components.CARD_WIDTH
import com.example.breezen.feature.meditation.components.GuidedMeditationCardWithEffect
import com.example.breezen.feature.meditation.model.createGuidedMeditationData



@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MeditationGuidedScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MeditationViewModel
) {



    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState)

    // Calculate padding so the first card is centered
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val contentPadding = (screenWidth - CARD_WIDTH) / 2

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF070707), Color(0xFF000000))))
            .padding(top = 48.dp, bottom = 40.dp)
    ) {

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Explore", fontFamily = FunnelDisplayFamily, fontSize = 54.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Peace", fontFamily = FunnelDisplayFamily, fontSize = 54.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
            Text("Through", fontFamily = Prata, fontSize = 48.sp, color = Color.LightGray.copy(alpha = 0.5f))
            Text("Practice", fontFamily = Prata, fontSize = 48.sp, color = Color.LightGray.copy(alpha = 0.5f))
        }

        Spacer(modifier = Modifier.weight(1f))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 100.dp),
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = contentPadding),
            horizontalArrangement = Arrangement.spacedBy((-80).dp)
        ) {
            items(8) { index ->

                // Create data 
                val meditationData = createGuidedMeditationData(
                    viewModel = viewModel,
                    index = index
                )

                GuidedMeditationCardWithEffect(
                    meditation = meditationData, 
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