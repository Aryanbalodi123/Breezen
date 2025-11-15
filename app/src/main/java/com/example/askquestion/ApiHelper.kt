package com.example.askquestion

import android.content.Context
import androidx.navigation.NavController
import com.example.askquestion.network.Song
import com.example.askquestion.ui.screens.TabViewModel

/**
 * Modern helper function to play a song.
 *
 * 1. Immediately navigates to the "player" screen (for instant UI response).
 * 2. Tells the ViewModel to handle all caching, streaming, and pre-loading in the background.
 */
fun playSongFromPlaylist(
    context: Context,
    viewModel: TabViewModel,
    selectedSong: Song,
    playlist: List<Song>,
    navController: NavController
) {
    if (playlist.isEmpty()) return

    // 1. Navigate immediately so the user sees the loading screen.
    navController.navigate("player")

    // 2. Tell the ViewModel to handle everything else (caching, streaming, etc.)
    viewModel.playSong(context, selectedSong, playlist)
}