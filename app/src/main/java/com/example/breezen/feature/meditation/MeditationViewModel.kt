package com.example.breezen.feature.meditation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.breezen.R
import com.example.breezen.core.network.GUIDED_AUDIO_BUCKET_URL
import com.example.breezen.core.ui.theme.pastelColors

class MeditationViewModel : ViewModel() {

    // Ideally this comes from a Repository, but keeping your structure for now
    val mp3ToTitle = listOf(
        listOf("still_echoes.mp3", "Echo - Of Stillness"),
        listOf("silent_descent.mp3", "Descent - Into Silence"),
        listOf("inner_ground.mp3", "Ground - Into Self"),
        listOf("gentle_breath.mp3", "Breathe - With Ease"),
        listOf("flow_presence.mp3", "Flow - With Presence"),
        listOf("whole_awareness.mp3", "Awareness - Of Everything"),
        listOf("warm_heart.mp3", "Heart - Of Kindness"),
        listOf("work_through.mp3", "Ease - Through Difficulty")
    )

    var currentSongIndex by mutableStateOf(0)
    var currentSongUrl by mutableStateOf("")

    // UI State for the player
    var passedColor by mutableStateOf(Color(0xFF88C0D0))
    var passedTitle by mutableStateOf("Flow State")
    var passedSubTitle by mutableStateOf("Presence")
    var passedVectorRes by mutableStateOf(R.drawable.three_d)

    val GlassBorder: Color
        get() = passedColor.copy(alpha = 0.3f)

    val GlassGradient: Brush
        get() = Brush.linearGradient(
            listOf(
                passedColor.copy(alpha = 0.15f),
                passedColor.copy(alpha = 0.03f)
            )
        )

    fun setAttributes(color: Color, title: String, subtitle: String, vectorRes: Int, currentIndex: Int, songUrl: String) {
        passedColor = color
        passedTitle = title
        passedSubTitle = subtitle
        passedVectorRes = vectorRes
        currentSongIndex = currentIndex
        // Assuming GUIDED_AUDIO_BUCKET_URL is defined in your network config
        this.currentSongUrl = GUIDED_AUDIO_BUCKET_URL + songUrl
    }

    fun skipToNext() {
        val nextIndex = (currentSongIndex + 1) % mp3ToTitle.size
        updateSong(nextIndex)
    }

    fun skipToPrevious() {
        val prevIndex = (currentSongIndex - 1 + mp3ToTitle.size) % mp3ToTitle.size
        updateSong(prevIndex)
    }

    private fun updateSong(index: Int) {
        val parts = mp3ToTitle[index][1].split(" - ")
        val title = parts.firstOrNull() ?: ""
        val subtitle = parts.getOrNull(1) ?: ""
        val songUrl = mp3ToTitle[index][0]

        // Update all state variables
        setAttributes(
            color = pastelColors.random(),
            title = title,
            subtitle = subtitle,
            vectorRes = passedVectorRes, // Keep current icon or randomize if you want
            currentIndex = index,
          songUrl = songUrl,
         // Pass the raw filename, setAttributes adds the URL prefix
        )
    }

    fun formatTime(ms: Long): String {
        val sec = ms / 1000
        return String.format("%02d:%02d", sec / 60, sec % 60)
    }
}