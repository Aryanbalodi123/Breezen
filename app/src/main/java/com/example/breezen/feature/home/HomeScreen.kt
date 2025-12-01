package com.example.breezen.feature.home

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.core.data.MoodPreference
import com.example.breezen.core.network.Song
import com.example.breezen.feature.chatbot.ChatViewModel
import com.example.breezen.feature.home.components.AffirmationSection
import com.example.breezen.feature.home.components.AppHeader
import com.example.breezen.feature.home.components.ChatBotSection
import com.example.breezen.feature.home.components.FeaturedSection
import com.example.breezen.feature.home.components.HeaderSection
import com.example.breezen.feature.home.components.MoodSelector
import com.example.breezen.feature.meditation.MeditationViewModel
import com.example.breezen.feature.music.TabViewModel

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun HomeContent(
    navController: NavController,
    viewModel: TabViewModel,
    meditationViewModel: MeditationViewModel,
    chatViewModel: ChatViewModel,
    homeViewModel: HomeViewModel
) {

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (viewModel.tabs.value.isEmpty()) {
            viewModel.fetchSongData(context)
        }
    }
    val tabs by viewModel.tabs
    val songs by viewModel.songs
    val isLoading = tabs.isEmpty()
    val user by homeViewModel.user

    LaunchedEffect(songs) {
        if (songs.isNotEmpty() && viewModel.headerSong == null) {
            val allSongs = songs.values.flatten()
            viewModel.headerSong = allSongs.randomOrNull()
            viewModel.featuredSongs = allSongs.shuffled().take(2)
        }
    }
    val isMoodDoneToday by MoodPreference.getMoodBoolean(context)
        .collectAsState(initial = false)

    val headerSong: Song? = viewModel.headerSong
    val featuredSongs: List<Song> = viewModel.featuredSongs

    Log.d("Music data", tabs.toString())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(bottom = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AppHeader(user?.username ?: "", navController)

            Spacer(modifier = Modifier.height(28.dp))

            HeaderSection(headerSong, viewModel, navController, isLoading, user?.username ?: "")

            Spacer(modifier = Modifier.height(28.dp))

            // This now handles its own state (UI vs Lottie)
            if(!isMoodDoneToday){MoodSelector()}

            Spacer(modifier = Modifier.height(28.dp))

            AffirmationSection()

            Spacer(modifier = Modifier.height(28.dp))

            FeaturedSection(
                navController, isLoading,
                viewModel = meditationViewModel
            )

            Spacer(modifier = Modifier.height(28.dp))

            ChatBotSection(chatViewModel , navController)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}