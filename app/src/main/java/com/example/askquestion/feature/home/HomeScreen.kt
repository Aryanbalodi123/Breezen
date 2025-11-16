package com.example.askquestion.feature.home

// --- THIS IS THE FIX ---
// --- END FIX ---
import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.askquestion.core.network.Song
import com.example.askquestion.feature.home.components.AffirmationSection
import com.example.askquestion.feature.home.components.AppHeader
import com.example.askquestion.feature.home.components.ChatBotSection
import com.example.askquestion.feature.home.components.FeaturedSection
import com.example.askquestion.feature.home.components.HeaderSection
import com.example.askquestion.feature.home.components.MoodSelector
import com.example.askquestion.feature.music.TabViewModel

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun HomeContent(
    navController: NavController,
    viewModel: TabViewModel,
    homeViewModel: HomeViewModel = viewModel()
) {

    val context = LocalContext.current // 1. Get the context
    LaunchedEffect(Unit) {
        if (viewModel.tabs.value.isEmpty()) {
            viewModel.fetchSongData(context) // 2. Pass the context here
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
                .padding( vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AppHeader(user?.username ?: "")

            Spacer(modifier = Modifier.height(28.dp))

            HeaderSection(headerSong, viewModel, navController, isLoading, user?.username ?: "")

            Spacer(modifier = Modifier.height(28.dp))
            MoodSelector()


            Spacer(modifier = Modifier.height(28.dp))

            AffirmationSection()


            Spacer(modifier = Modifier.height(28.dp))

            FeaturedSection(navController, isLoading, viewModel, featuredSongs)

            Spacer(modifier = Modifier.height(28.dp))

            ChatBotSection()

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}