package com.example.breezen.feature.meditation.model

import androidx.compose.ui.graphics.Color
import com.example.breezen.R
import com.example.breezen.core.ui.theme.pastelColors
import com.example.breezen.feature.meditation.MeditationViewModel

fun createGuidedMeditationData(
    viewModel: MeditationViewModel,
    index: Int
): GuidedMeditation {

    val vectorDrawables = listOf(
        R.drawable.pebble,
        R.drawable.ring,
        R.drawable.tablet_broken,
        R.drawable.v_component,
        R.drawable.three_d,
        R.drawable.triangle,
        R.drawable.three_circle
    )

    // Ensures no two consecutive cards use the same pastel color
    val shuffledColors = pastelColors.shuffled().toMutableList()
    for (i in 1 until shuffledColors.size) {
        if (shuffledColors[i] == shuffledColors[i - 1]) {
            shuffledColors[i] = pastelColors.first { it != shuffledColors[i - 1] }
        }
    }

    val backgroundColor = shuffledColors[index % pastelColors.size]
    val shade = pastelShade(backgroundColor, 0.55f)
    val vector = vectorDrawables[index % vectorDrawables.size]

    // Extract title and subtitle from your mp3 mapping
    val (rawTitle, rawSubtitle) = viewModel.mp3ToTitle[index][1]
        .split(" - ")
        .let { parts ->
            (parts.firstOrNull() ?: "") to (parts.getOrNull(1) ?: "")
        }

    return GuidedMeditation(
        title = rawTitle,
        subtitle = rawSubtitle,
        backgroundColor = backgroundColor,
        titleColor = Color.Black,
        secondaryColor = shade,
        vectorResId = vector,
        currentIndex = index
    )
}
