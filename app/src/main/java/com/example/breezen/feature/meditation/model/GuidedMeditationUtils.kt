import androidx.compose.ui.graphics.Color
import com.example.breezen.R
import com.example.breezen.core.ui.theme.pastelColors
import com.example.breezen.feature.meditation.MeditationViewModel
import com.example.breezen.feature.meditation.model.GuidedMeditation
import com.example.breezen.feature.meditation.model.pastelShade

fun createGuidedMeditationData(
    viewModel: MeditationViewModel,
    index: Int
): GuidedMeditation {

    val vectorDrawables = listOf(
        R.drawable.pebble, R.drawable.ring, R.drawable.tablet_broken,
        R.drawable.v_component, R.drawable.three_d, R.drawable.triangle,
        R.drawable.three_circle
    )

    // NO remember here, because not composable
    val shuffledColors = pastelColors.shuffled().toMutableList()
    for (i in 1 until shuffledColors.size) {
        if (shuffledColors[i] == shuffledColors[i - 1]) {
            shuffledColors[i] = pastelColors.first { it != shuffledColors[i - 1] }
        }
    }

    val vector = vectorDrawables[index % 7]
    val baseColor = shuffledColors[index % pastelColors.size]
    val shade = pastelShade(baseColor, 0.55f)
    val parts = viewModel.mp3ToTitle[index][1].split(" - ")

    return GuidedMeditation(
        title = parts.firstOrNull() ?: "",
        subtitle = parts.getOrNull(1) ?: "",
        backgroundColor = baseColor,
        titleColor = Color.Black,
        secondaryColor = shade,
        vectorResId = vector,
        currentIndex = index
    )
}
