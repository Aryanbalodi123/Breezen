package com.example.breezen.feature.home

import android.annotation.SuppressLint
import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.core.data.MoodPreference
import com.example.breezen.core.network.Song
import com.example.breezen.core.ui.theme.AppBlack
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

    /**
     * Loads song tabs + metadata once when the screen appears.
     * Required to hydrate all sections dependent on song data.
     */
    LaunchedEffect(Unit) {
        if (viewModel.tabs.value.isEmpty()) {
            viewModel.fetchSongData(context)
        }
    }

    val tabs by viewModel.tabs
    val songs by viewModel.songs
    val user by homeViewModel.user
    val isLoading = tabs.isEmpty()

    /**
     * Once songs arrive, choose header song + two featured songs.
     * Triggers only when songs list changes.
     */
    LaunchedEffect(songs) {
        if (songs.isNotEmpty() && viewModel.headerSong == null) {
            val allSongs = songs.values.flatten()
            viewModel.headerSong = allSongs.randomOrNull()
            viewModel.featuredSongs = allSongs.shuffled().take(2)
        }
    }

    /**
     * Observe daily mood completion state.
     * Controls visibility of MoodSelector.
     */
    val isMoodDoneToday by MoodPreference
        .observeMoodState(context)
        .collectAsState(initial = false)

    val headerSong: Song? = viewModel.headerSong

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBlack)
            .padding(bottom = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Top app bar (logo + avatar)
            AppHeader(
                username = user?.username ?: "",
                navController = navController
            )

            Spacer(Modifier.height(28.dp))

            // Music banner with gradient background + play button
            HeaderSection(
                song = headerSong,
                viewModel = viewModel,
                navController = navController,
                isLoading = isLoading,
                username = user?.username ?: ""
            )

            Spacer(Modifier.height(28.dp))

            // Mood picker shown once per day
            if (!isMoodDoneToday) {
                MoodSelector()
            }

            Spacer(Modifier.height(28.dp))

            // Swipeable inspirational cards
            AffirmationSection()

            Spacer(Modifier.height(28.dp))

            // Two recommended meditation cards
            FeaturedSection(
                navController = navController,
                viewModel = meditationViewModel
            )

            Spacer(Modifier.height(28.dp))

            // Chatbot quick-entry section
            ChatBotSection(
                chatViewModel = chatViewModel,
                navController = navController
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}
