package com.example.breezen.feature.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.core.data.MoodPreference
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
import kotlinx.coroutines.delay

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

    // 1. Observe Data from ViewModels
    val tabs by viewModel.tabs
    // We use allSongs directly if available, otherwise flatten the map
    val songsMap by viewModel.songs

    // Assuming homeViewModel.user is a State<User?> or similar
    val user by homeViewModel.user

    val isLoading = tabs.isEmpty()

    // 2. Initial Data Fetch
    LaunchedEffect(Unit) {
        if (viewModel.tabs.value.isEmpty()) {
            viewModel.fetchSongData(context)
        }
    }

    // 3. Logic to pick a random Header Song & Featured Songs
    // This runs only when songs are loaded and header is still null
    LaunchedEffect(songsMap) {
        if (songsMap.isNotEmpty() && viewModel.headerSong == null) {
            val allSongsList = songsMap.values.flatten()
            if (allSongsList.isNotEmpty()) {
                viewModel.headerSong = allSongsList.randomOrNull()
                viewModel.featuredSongs = allSongsList.shuffled().take(2)
            }
        }
    }

    // 4. Observe Mood State (Flow)
    val isMoodDoneToday by MoodPreference
        .observeMoodState(context)
        .collectAsState(initial = null)

    // Access the property directly (assuming it is backed by mutableStateOf in VM)
    val headerSong = viewModel.headerSong

    var showMoodSelector by remember { mutableStateOf(false) }
    var showRestOfContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Wait for Header Animation to mostly finish (HeaderSection takes ~1.5s)
        delay(2000)
        showMoodSelector = true

        // Slight delay before showing affirmations to create a cascading effect
        delay(600)
        showRestOfContent = true
    }

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

            // Top Header
            AppHeader(
                username = user?.username ?: "User",
                navController = navController
            )

            Spacer(Modifier.height(28.dp))

            // Music Player Banner
            HeaderSection(
                song = headerSong,
                viewModel = viewModel,
                navController = navController,
                isLoading = isLoading,
                username = user?.username ?: ""
            )

            Spacer(Modifier.height(28.dp))

            // Daily Mood Selector (Only if not done today)
            if (showMoodSelector && isMoodDoneToday == false) {
                // MoodSelector has its own internal animation, so we just trigger it here
                MoodSelector()
                Spacer(Modifier.height(28.dp))
            }

            // Rest of the content (Affirmations, Featured, Chatbot)
            AnimatedVisibility(
                visible = showRestOfContent,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { it / 4 }
            ) {
                Column {
                    // Affirmations
                    AffirmationSection()

                    Spacer(Modifier.height(28.dp))

                    // Featured Meditations
                    FeaturedSection(
                        navController = navController,
                        viewModel = meditationViewModel
                    )

                    Spacer(Modifier.height(28.dp))

                    // ChatBot Entry
                    ChatBotSection(
                        chatViewModel = chatViewModel,
                        navController = navController
                    )

                    Spacer(Modifier.height(64.dp))
                }
            }
        }
    }
}