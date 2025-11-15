import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.askquestion.R
import com.example.askquestion.theme.CustomTypography
import kotlin.math.absoluteValue

@Composable
fun MeditateScreen(navController: NavController) {
    val totalCards = 5
    val pagerState = rememberPagerState(pageCount = { totalCards })
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(all = 10.dp)
    ) {
        VerticalPager(
            state = pagerState,
            contentPadding = PaddingValues(
                start = 0.dp,
                top = 0.dp,
                end = 0.dp,
                bottom = 80.dp
            ),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // Calculate page offset for animations
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val scale = lerp(0.95f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
            val alpha = lerp(0.7f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth() // Take full available size
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = "Meditation Card $page",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Popular",
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = CustomTypography.titleMedium
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "Recommended",
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = CustomTypography.titleMedium

                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(
                        painterResource(R.drawable.play),
                        null,
                        Modifier.size(64.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.weight(1f))

                    Column(Modifier
                        .fillMaxWidth()
                        .padding(24.dp)) {
                        Text(
                            text = "Distance city bundle.",
                            style = CustomTypography.displayLarge
                        )

                        Text("'SLDFK KLW FLW KF KJWFN GEJKNKJNDV ENER VNEKJ VEIFJV IJEFNVKJ EVKJERKJV RKJGNERKJ RNERNEVJRENVJ ")
                    }
                }

            }
        }
    }
}

// Helper function for linear interpolation
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}