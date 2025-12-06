package com.example.breezen.feature.meditation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.breezen.R
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

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val contentPadding = (screenWidth - CARD_WIDTH) / 2



    var showExplore by remember { mutableStateOf(false) }
    var showPeace by remember { mutableStateOf(false) }
    var showThrough by remember { mutableStateOf(false) }
    var showPractice by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showExplore = true
        kotlinx.coroutines.delay(120)
        showPeace = true
        kotlinx.coroutines.delay(120)
        showThrough = true
        kotlinx.coroutines.delay(120)
        showPractice = true
    }

  Box(Modifier.fillMaxSize()){
          Image(painter = painterResource(R.drawable.meditation_bg) , contentDescription = null, contentScale = ContentScale.Crop,modifier = Modifier.fillMaxSize())

      Column(
          modifier = modifier
              .fillMaxSize()

              .padding(top = 48.dp, bottom = 40.dp)
      )
      {
          Column(modifier = Modifier.padding(horizontal = 24.dp)) {

              AnimatedVisibility(
                  visible = showExplore,
                  enter = fadeIn(tween(650)) +
                          slideInVertically(
                              animationSpec = tween(
                                  650,
                                  easing = androidx.compose.animation.core.CubicBezierEasing(
                                      0.2f, 0.9f, 0.3f, 1.2f
                                  )
                              )
                          ) { it / 2 }
              ) {
                  Text("Explore", fontFamily = FunnelDisplayFamily, fontSize = 54.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }

              AnimatedVisibility(
                  visible = showPeace,
                  enter = fadeIn(tween(650)) +
                          slideInVertically(
                              animationSpec = tween(
                                  650,
                                  easing = androidx.compose.animation.core.CubicBezierEasing(
                                      0.2f, 0.9f, 0.3f, 1.2f
                                  )
                              )
                          ) { it / 2 }
              ) {
                  Text("Peace", fontFamily = FunnelDisplayFamily, fontSize = 54.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }

              AnimatedVisibility(
                  visible = showThrough,
                  enter = fadeIn(tween(650)) +
                          slideInVertically(
                              animationSpec = tween(
                                  650,
                                  easing = androidx.compose.animation.core.CubicBezierEasing(
                                      0.2f, 0.9f, 0.3f, 1.2f
                                  )
                              )
                          ) { it / 2 }
              ) {
                  Text("Through", fontFamily = Prata, fontSize = 48.sp, color = Color.LightGray.copy(alpha = 0.5f))
              }

              AnimatedVisibility(
                  visible = showPractice,
                  enter = fadeIn(tween(650)) +
                          slideInVertically(
                              animationSpec = tween(
                                  650,
                                  easing = androidx.compose.animation.core.CubicBezierEasing(
                                      0.2f, 0.9f, 0.3f, 1.2f
                                  )
                              )
                          ) { it / 2 }
              ) {
                  Text("Practice", fontFamily = Prata, fontSize = 48.sp, color = Color.LightGray.copy(alpha = 0.5f))
              }
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
}
