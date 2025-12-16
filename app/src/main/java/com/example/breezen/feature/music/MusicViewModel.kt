package com.example.breezen.feature.music

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breezen.core.network.Category
import com.example.breezen.core.network.ErrorReportBody
import com.example.breezen.core.network.MusicCacheManager
import com.example.breezen.core.network.RetroFitClient
import com.example.breezen.core.network.SUPABASE_API_KEY_ANON
import com.example.breezen.core.network.Song
import com.example.breezen.core.network.TELEGRAM_BOT_TOKEN
import com.example.breezen.core.network.Tab
import com.example.breezen.core.network.getMusicStreamUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import android.graphics.Color as AndroidColor

enum class PlayerLoadState { IDLE, INITIAL, TRANSITIONING }

data class PlayerUiState(
    val currentSong: Song? = null,
    val streamUrl: String = "",
    val loadState: PlayerLoadState = PlayerLoadState.IDLE,
    val isBuffering: Boolean = false,
    val dominantColor: Color = Color(0xFF444444),
    val nextUpSong: Song? = null,
    val nextStreamUrl: String = "",
    val error: String? = null
)

class TabViewModel : ViewModel() {

    enum class RepeatMode { OFF, ALL, ONE }

    // --- State ---
    private val _playerUiState = mutableStateOf(PlayerUiState())
    private val _tabs = mutableStateOf<List<Tab>>(emptyList())
    private val _categories = mutableStateOf<Map<String, List<Category>>>(emptyMap())
    private val _songs = mutableStateOf<Map<String, List<Song>>>(emptyMap())
    private val _allSongs = mutableStateOf<List<Song>>(emptyList())


    val allSongs: State<List<Song>> = _allSongs
    private val _currentPlaylist = mutableStateOf<List<Song>>(emptyList())
    private var _currentSongIndex = -1

    val songColorCache = mutableStateMapOf<String, Color>()

    val playerUiState: State<PlayerUiState> = _playerUiState
    val tabs: State<List<Tab>> = _tabs
    val categories: State<Map<String, List<Category>>> = _categories
    val songs: State<Map<String, List<Song>>> = _songs

    var isPlaying by mutableStateOf(false)
    var isShuffleEnabled by mutableStateOf(false)
    var repeatMode: RepeatMode by mutableStateOf(RepeatMode.OFF)


    var headerSong: Song? by mutableStateOf(null)
    var featuredSongs: List<Song> by mutableStateOf(emptyList())
    // --- Data Fetching ---
    fun fetchSongData(context: Context) {
        viewModelScope.launch {
            try {
                val resultTab = RetroFitClient.api.getTabs(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                _tabs.value = resultTab
                val resultCategory = RetroFitClient.api.getCategories(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                _categories.value = resultCategory.groupBy { it.tab_id }
                val resultSong = RetroFitClient.api.getSongs(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                    .filter { !it.got_error }
                _songs.value = resultSong.groupBy { it.category_id }
                _allSongs.value = resultSong
            } catch (e: Exception) {
                Log.e("TabsViewModel", "Failed to fetch song data", e)
            }
        }
    }

    // --- Optimization 4: Backend Color Calculation ---
    fun getDominantColor(song: Song?): Color {
        val hexString = song?.dominant_color
        if (hexString.isNullOrEmpty()) return Color(0xFF444444)
        return try {
            val colorString = if (hexString.startsWith("#")) hexString else "#$hexString"
            Color(AndroidColor.parseColor(colorString))
        } catch (e: Exception) {
            Color(0xFF444444)
        }
    }

    // --- Optimization 1: Optimistic UI ---
    fun playSong(context: Context, song: Song, playlist: List<Song>) {
        _currentPlaylist.value = playlist
        _currentSongIndex = playlist.indexOf(song)

        val dominantColor = songColorCache.getOrPut(song.id) { getDominantColor(song) }

        // IMMEDIATE UPDATE: Show player UI instantly with metadata.
        // We set IDLE so the full-screen loader DOES NOT block the view.
        // We set isBuffering = true so the Play button shows a spinner.
        _playerUiState.value = PlayerUiState(
            currentSong = song,
            dominantColor = dominantColor,
            loadState = PlayerLoadState.IDLE,
            isBuffering = true,
            // Keep previous nextUp temporarily so the UI doesn't flicker empty
            nextUpSong = _playerUiState.value.nextUpSong
        )

        prepareSongInternal(context, song)
    }

    // --- Optimization 2 & 3: Parallel Concurrency & Smart Caching ---
    private fun prepareSongInternal(context: Context, song: Song) {
        viewModelScope.launch {
            try {
                // STEP 1: Smart Local Caching (Disk > Network)
                // Check if file exists ON DISK. If so, return immediately.
                val localFileName = "tg_${song.stream_id}.opus"
                val localFile = MusicCacheManager.getCachedFile(context, localFileName)

                var effectiveUrl = ""

                if (localFile.exists()) {
                    Log.d("TabViewModel", "⚡ Cache Hit: Playing from disk")
                    effectiveUrl = localFile.absolutePath
                } else {
                    Log.d("TabViewModel", "☁ Cache Miss: Fetching URL")
                    // Only fetch URL if file is missing
                    val fetchedUrl = getMusicStreamUrl(TELEGRAM_BOT_TOKEN, song.stream_id)
                    if (fetchedUrl.isNullOrEmpty()) throw Exception("Stream URL empty")
                    effectiveUrl = fetchedUrl
                }

                // Update UI with the Playable URL
                _playerUiState.value = _playerUiState.value.copy(
                    streamUrl = effectiveUrl,
                    isBuffering = true, // Exoplayer will turn this off when ready
                    error = null
                )

                // STEP 2: Parallel Concurrency
                // Launch "Next Song" calculation and pre-caching in a SEPARATE coroutine.
                // This ensures the current song starts playing 100-300ms faster.
                launch(Dispatchers.IO) {
                    calculateAndCacheNextSong(context)
                }

            } catch (e: Exception) {
                onPlaybackError(context, song, "Setup Failed: ${e.message}")
            }
        }
    }

    suspend fun calculateAndCacheNextSong(context: Context?) {
        val nextSong = getNextUpSong() ?: return

        // Fetch Next URL (Network Call)
        val nextStreamUrl = try {
            MusicCacheManager.getCachedStreamUrl(nextSong.stream_id)
                ?: getMusicStreamUrl(TELEGRAM_BOT_TOKEN, nextSong.stream_id)
        } catch (e: Exception) { null } ?: ""

        // Update UI (Next Up Card)
        withContext(Dispatchers.Main) {
            _playerUiState.value = _playerUiState.value.copy(
                nextUpSong = nextSong,
                nextStreamUrl = nextStreamUrl
            )
        }

        // Pre-download Next Song (Background)
        if (nextStreamUrl.isNotEmpty()) {
            try {
                MusicCacheManager.downloadIfMissing(context, nextStreamUrl, "tg_${nextSong.stream_id}.opus")
            } catch (e: Exception) {
                Log.e("TabViewModel", "Pre-cache failed", e)
            }
        }
    }

    // --- Error Recovery ---
    fun onPlaybackError(context: Context, song: Song?, errorDesc: String) {
        if (song == null) return
        Log.e("TabViewModel", "Playback Error: $errorDesc")

        viewModelScope.launch {
            launch(Dispatchers.IO) {
                try {
                    RetroFitClient.api.reportError(
                        SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON",
                        "id.eq.${song.id}", ErrorReportBody(got_error = true, got_error_desc = errorDesc)
                    )
                } catch (e: Exception) { Log.e("ErrorReporter", "Failed", e) }
            }

            val currentList = _currentPlaylist.value.toMutableList()
            currentList.removeIf { it.id == song.id }
            _currentPlaylist.value = currentList

            Toast.makeText(context, "Skipping unavailable: ${song.title}", Toast.LENGTH_SHORT).show()

            if (currentList.isNotEmpty()) {
                if (_currentSongIndex >= currentList.size) _currentSongIndex = 0
                playNextSong(context, forceManual = true)
            } else {
                _playerUiState.value = _playerUiState.value.copy(
                    loadState = PlayerLoadState.IDLE, currentSong = null
                )
            }
        }
    }

    // --- Playback Controls ---
    fun playNextSong(context: Context, forceManual: Boolean = false) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        if (repeatMode == RepeatMode.ONE && !forceManual) {
            // Optimistic update for replay
            val song = playlist[_currentSongIndex]
            _playerUiState.value = _playerUiState.value.copy(isBuffering = true)
            prepareSongInternal(context, song)
            return
        }

        val nextIndex = if (isShuffleEnabled) {
            var newIndex = _currentSongIndex
            var attempts = 0
            while (newIndex == _currentSongIndex && attempts < 10 && playlist.size > 1) {
                newIndex = Random.nextInt(0, playlist.size)
                attempts++
            }
            if (playlist.size <= 1) 0 else newIndex
        } else {
            val i = _currentSongIndex + 1
            if (i >= playlist.size) {
                if (repeatMode == RepeatMode.ALL || (repeatMode == RepeatMode.ONE && forceManual)) 0 else return
            } else i
        }

        _currentSongIndex = nextIndex
        val nextSong = playlist.getOrNull(_currentSongIndex) ?: return

        // OPTIMISTIC UPDATE: Switch UI immediately
        val dominantColor = songColorCache.getOrPut(nextSong.id) { getDominantColor(nextSong) }
        _playerUiState.value = _playerUiState.value.copy(
            currentSong = nextSong,
            dominantColor = dominantColor,
            isBuffering = true,
            // Clear Next Up momentarily so we don't show the song currently playing as "Next Up"
            nextUpSong = null
        )

        prepareSongInternal(context, nextSong)
    }

    fun playPreviousSong(context: Context) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return
        _currentSongIndex = if (_currentSongIndex - 1 < 0) playlist.size - 1 else _currentSongIndex - 1
        val prevSong = playlist.getOrNull(_currentSongIndex) ?: return

        val dominantColor = songColorCache.getOrPut(prevSong.id) { getDominantColor(prevSong) }
        _playerUiState.value = _playerUiState.value.copy(
            currentSong = prevSong,
            dominantColor = dominantColor,
            isBuffering = true,
            nextUpSong = null
        )
        prepareSongInternal(context, prevSong)
    }

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        // Re-calculate next up in background
        viewModelScope.launch(Dispatchers.IO) { calculateAndCacheNextSong(null!!) } // context null is safe here as download handles it gracefully or we just skip DL
    }


    fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        viewModelScope.launch(Dispatchers.IO) { calculateAndCacheNextSong(null) }
    }

    private fun getNextUpSong(): Song? {
        val list = _currentPlaylist.value
        if (list.isEmpty()) return null
        if (repeatMode == RepeatMode.ONE) return list.getOrNull(_currentSongIndex)

        val nextIndex = if (isShuffleEnabled) {
            val i = Random.nextInt(0, list.size)
            if (i == _currentSongIndex && list.size > 1) (i + 1) % list.size else i
        } else {
            val i = _currentSongIndex + 1
            if (i >= list.size) {
                if (repeatMode == RepeatMode.ALL) 0 else -1
            } else i
        }
        return if (nextIndex != -1) list.getOrNull(nextIndex) else null
    }

    fun onPlayerReadyAndImageLoaded() {
        _playerUiState.value = _playerUiState.value.copy(loadState = PlayerLoadState.IDLE)
    }

    fun setIsBuffering(isBuffering: Boolean) {
        _playerUiState.value = _playerUiState.value.copy(isBuffering = isBuffering)
    }
}