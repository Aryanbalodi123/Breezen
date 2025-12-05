package com.example.breezen.feature.music.utils

import android.content.Context
import androidx.navigation.NavController
import com.example.breezen.core.network.Song
import com.example.breezen.feature.music.TabViewModel

/**
 * Coordinates the transition to the player screen.
 * Initiates navigation for immediate UI feedback and delegates media preparation
 * (caching, streaming setup) to the ViewModel.
 */
fun playSongFromPlaylist(
    context: Context,
    viewModel: TabViewModel,
    selectedSong: Song,
    playlist: List<Song>,
    navController: NavController
) {
    if (playlist.isEmpty()) return

    // Trigger navigation immediately to minimize perceived latency for the user
    navController.navigate("player")

    // Delegate media playback initialization and playlist management to the ViewModel
    // This handles background buffering, audio focus, and session state
    viewModel.playSong(context, selectedSong, playlist)
}