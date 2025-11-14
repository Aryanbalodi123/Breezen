package com.example.askquestion

import android.content.Context
import androidx.navigation.NavController
import com.example.askquestion.network.Song
import com.example.askquestion.ui.screens.TabViewModel

/**
 * Immediately navigates to the player and tells the ViewModel to
 * prepare the selected song from the given playlist.
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

    // 2. Tell the ViewModel to handle everything else in the background.
    viewModel.playSong(context, selectedSong, playlist)
}