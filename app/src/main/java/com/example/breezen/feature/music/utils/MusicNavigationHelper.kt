package com.example.breezen.feature.music.utils

import android.content.Context
import androidx.navigation.NavController
// --- THIS IS THE FIX ---
import com.example.breezen.core.network.Song
// --- END FIX ---
import com.example.breezen.feature.music.TabViewModel

/**
 * Modern helper function to play a song.
 *
 * 1. Immediately navigates to the "player" screen (for instant UI response).
 * 2. Tells the ViewModel to handle all caching, streaming, and pre-loading in the background.
 */
fun playSongFromPlaylist(
    context: Context,
    viewModel: TabViewModel,
    selectedSong: Song, // Type is now core.network.Song
    playlist: List<Song>, // Type is now List<core.network.Song>
    navController: NavController
) {
    if (playlist.isEmpty()) return

    // 1. Navigate immediately so the user sees the loading screen.
    navController.navigate("player")

    // 2. Tell the ViewModel to handle everything else (caching, streaming, etc.)
    viewModel.playSong(context, selectedSong, playlist)
}