package com.example.askquestion

import android.content.Context
import android.util.Log
import androidx.navigation.NavController
import com.example.askquestion.network.Song
import com.example.askquestion.network.TELEGRAM_BOT_TOKEN
import com.example.askquestion.network.retrieveMusicFile
import com.example.askquestion.ui.screens.TabViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun playGetMusicFile(
    context: Context,
    viewModel: TabViewModel,
    musicItem: Song?,
    coroutineScope: CoroutineScope,
    navController: NavController
) {
    if (musicItem == null) return

    coroutineScope.launch {
        val musicFilePath = retrieveMusicFile(context, TELEGRAM_BOT_TOKEN, musicItem.stream_id)

        if (musicFilePath != null) {
            viewModel.setCurrentSong(context, musicItem, musicFilePath)
            navController.navigate("player")

        } else {
            Log.e("MusicUtils", "Failed to fetch music file for ${musicItem.title}")
        }
    }
}
