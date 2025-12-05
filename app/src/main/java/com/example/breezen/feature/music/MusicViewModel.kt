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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import androidx.compose.runtime.mutableStateOf as mutableStateOf2

/**
 * Defines the loading state of the media player UI.
 * IDLE: Player is ready or stopped.
 * INITIAL: A new song has been selected and setup is starting.
 * TRANSITIONING: Moving between tracks or states.
 */
enum class PlayerLoadState { IDLE, INITIAL, TRANSITIONING }

/**
 * Represents the current state of the Player UI.
 * Holds reference to the active song, stream URLs, buffering status, and the calculated next track.
 */
data class PlayerUiState(
    val currentSong: Song? = null,
    val streamUrl: String = "",
    val loadState: PlayerLoadState = PlayerLoadState.INITIAL,
    val isBuffering: Boolean = false,
    val dominantColor: Color = Color(0xFF444444),
    val nextUpSong: Song? = null,
    val nextStreamUrl: String = "",
    val error: String? = null // UI State error field
)

/**
 * ViewModel responsible for managing music data (Tabs, Categories, Songs) and the Media Player state.
 * Handles data fetching, playlist management, shuffle/repeat logic, and error reporting.
 */
class TabViewModel : ViewModel() {

    enum class RepeatMode { OFF, ALL, ONE }

    // --- Private Mutable State (Source of Truth) ---
    private val _playerUiState = mutableStateOf(PlayerUiState())
    private val _tabs = mutableStateOf<List<Tab>>(emptyList())
    private val _categories = mutableStateOf<Map<String, List<Category>>>(emptyMap())
    private val _songs = mutableStateOf<Map<String, List<Song>>>(emptyMap())
    private val _allSongs = mutableStateOf<List<Song>>(emptyList())

    // Internal playlist state management
    private val _currentPlaylist = mutableStateOf<List<Song>>(emptyList())
    private var _currentSongIndex = -1

    // Caches parsed colors to avoid repetitive hex parsing overhead
    val songColorCache = mutableStateMapOf<String, Color>()

    // --- Public Immutable State (Exposed to UI) ---
    val playerUiState: State<PlayerUiState> = _playerUiState
    val tabs: State<List<Tab>> = _tabs
    val categories: State<Map<String, List<Category>>> = _categories
    val songs: State<Map<String, List<Song>>> = _songs
    val allSongs: State<List<Song>> = _allSongs

    // Header/Featured content state
    var headerSong: Song? by mutableStateOf2(null)
    var featuredSongs: List<Song> by mutableStateOf2(emptyList())

    // Player control toggles
    var isPlaying by mutableStateOf2(false)
    var isShuffleEnabled by mutableStateOf2(false)
    var repeatMode: RepeatMode by mutableStateOf2(RepeatMode.OFF)

    /**
     * Initializes the app data by fetching Tabs, Categories, and Songs from Supabase.
     * Songs are filtered to exclude those marked with 'got_error' in the database.
     */
    fun fetchSongData(context: Context) {
        viewModelScope.launch {
            try {
                // Fetch Tabs
                val resultTab = RetroFitClient.api.getTabs(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                _tabs.value = resultTab

                // Fetch and Group Categories
                val resultCategory = RetroFitClient.api.getCategories(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                _categories.value = resultCategory.groupBy { it.tab_id }

                // Fetch Songs and apply client-side safety filter
                // API already filters got_error=false via query, but safety filter here too
                val resultSong = RetroFitClient.api.getSongs(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                    .filter { !it.got_error }

                // Group songs by category for the UI grids
                _songs.value = resultSong.groupBy { it.category_id }
                _allSongs.value = resultSong

            } catch (e: Exception) {
                Log.e("TabsViewModel", "Failed to fetch song data", e)
            }
        }
    }

    /**
     * Helper to parse the hex color string from the DB.
     * Falls back to dark grey on failure.
     */
    fun getDominantColor(song: Song?): Color {
        val raw = song?.dominant_color ?: return Color(0xFF444444)
        return try {
            Color(android.graphics.Color.parseColor("#$raw"))
        } catch (e: Exception) {
            Color(0xFF444444)
        }
    }

    /**
     * Primary entry point for playing a song.
     * Sets the current playlist context, updates the UI state, and triggers media preparation.
     */
    fun playSong(context: Context, song: Song, playlist: List<Song>) {
        _currentPlaylist.value = playlist
        _currentSongIndex = playlist.indexOf(song)

        val dominantColor = songColorCache[song.id] ?: getDominantColor(song)

        _playerUiState.value = PlayerUiState(
            loadState = PlayerLoadState.INITIAL,
            dominantColor = dominantColor
        )
        prepareSongInternal(context, song, PlayerLoadState.INITIAL)
    }

    // --- NEW: Error Reporting ---
    /**
     * reports playback failures to Supabase so bad tracks can be flagged/hidden globally.
     */
    private fun reportPlaybackError(song: Song, errorDesc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetroFitClient.api.reportError(
                    SUPABASE_API_KEY_ANON,
                    "Bearer $SUPABASE_API_KEY_ANON",
                    "id.eq.${song.id}", // Supabase query syntax
                    ErrorReportBody(got_error = true, got_error_desc = errorDesc)
                )
                Log.d("ErrorReporter", "Reported error for song ${song.id}")
            } catch (e: Exception) {
                Log.e("ErrorReporter", "Failed to report error", e)
            }
        }
    }

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        viewModelScope.launch { updateNextUp() }
    }

    fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        viewModelScope.launch { updateNextUp() }
    }

    /**
     * recalculates the "Next Up" song based on shuffle/repeat settings
     * and fetches its URL so the UI can display it.
     */
    private suspend fun updateNextUp() {
        val nextSong = getNextUpSong()
        val nextStreamUrl = nextSong?.let {
            getMusicStreamUrl(TELEGRAM_BOT_TOKEN, it.stream_id)
        } ?: ""
        _playerUiState.value = _playerUiState.value.copy(
            nextUpSong = nextSong,
            nextStreamUrl = nextStreamUrl
        )
    }

    fun onPlayerReadyAndImageLoaded() {
        _playerUiState.value = _playerUiState.value.copy(loadState = PlayerLoadState.IDLE)
    }

    fun setIsBuffering(isBuffering: Boolean) {
        _playerUiState.value = _playerUiState.value.copy(isBuffering = isBuffering)
    }

    /**
     * Core logic for preparing media.
     * 1. Fetches the direct stream URL (Telegram API).
     * 2. Checks local cache for downloaded files.
     * 3. Pre-fetches (caches) the *next* song to ensure gapless playback.
     * 4. Handles critical errors (Bad URL/Network) by reporting to DB and auto-skipping.
     */
    private fun prepareSongInternal(context: Context, song: Song, triggeredBy: PlayerLoadState) {
        viewModelScope.launch {
            try {
                // 1. Asynchronously fetch Stream URL and calculate Next Song
                val streamUrlDeferred = async { getMusicStreamUrl(TELEGRAM_BOT_TOKEN, song.stream_id) }
                val nextSongDeferred = async { getNextUpSong() }

                val musicStreamUrl = streamUrlDeferred.await()
                val nextSong = nextSongDeferred.await()

                // Check for failure in fetching URL
                if (musicStreamUrl.isNullOrEmpty()) {
                    throw Exception("Stream URL is null or empty")
                }

                // 2. Check Cache: If file exists locally, use path. Else use Network URL.
                val localFile = MusicCacheManager.getCachedFile(context, "tg_${song.stream_id}.opus")
                    .takeIf { it.exists() }?.absolutePath
                val effectiveUrl = localFile ?: musicStreamUrl

                // 3. Pre-cache optimization: Download the *next* song in the background
                nextSong?.let { ns ->
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val cachedNextUrl = MusicCacheManager.getCachedStreamUrl(ns.stream_id)
                                ?: getMusicStreamUrl(TELEGRAM_BOT_TOKEN, ns.stream_id)
                            if (!cachedNextUrl.isNullOrEmpty()) {
                                MusicCacheManager.downloadIfMissing(context, cachedNextUrl, "tg_${ns.stream_id}.opus")
                            }
                        } catch (ignored: Exception) {}
                    }
                }

                val nextStreamUrl = nextSong?.let {
                    MusicCacheManager.getCachedStreamUrl(it.stream_id) ?: getMusicStreamUrl(TELEGRAM_BOT_TOKEN, it.stream_id)
                } ?: ""

                // Update UI state with valid media data
                _playerUiState.value = _playerUiState.value.copy(
                    currentSong = song,
                    streamUrl = effectiveUrl,
                    loadState = triggeredBy,
                    isBuffering = true,
                    nextUpSong = nextSong,
                    nextStreamUrl = nextStreamUrl,
                    error = null
                )

            } catch (e: Exception) {
                // --- ERROR HANDLING IMPLEMENTATION ---
                Log.e("TabViewModel", "Critical Error in prepareSongInternal", e)

                // 1. Report the error to the database so it can be fixed
                reportPlaybackError(song, e.message ?: "Unknown playback error")

                // 2. Remove the broken song from the current playlist instance
                val filteredList = _currentPlaylist.value.filter { it.id != song.id }
                _currentPlaylist.value = filteredList

                // 3. Notify User
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error playing ${song.title}. Removing from playlist.", Toast.LENGTH_SHORT).show()
                }

                // 4. Recovery: Auto-skip to the next available song
                if (filteredList.isNotEmpty()) {
                    playNextSong(context, forceManual = true)
                } else {
                    // Playlist is empty/dead, reset player state
                    _playerUiState.value = _playerUiState.value.copy(loadState = PlayerLoadState.IDLE, currentSong = null)
                }
            }
        }
    }

    /**
     * Calculates the index of the next song based on Shuffle and Repeat modes.
     */
    private fun getNextIndex(peek: Boolean = false): Int {
        if (_currentPlaylist.value.isEmpty()) return -1
        when (repeatMode) {
            RepeatMode.ONE -> return _currentSongIndex
            RepeatMode.ALL, RepeatMode.OFF -> {
                if (isShuffleEnabled) {
                    if (_currentPlaylist.value.size <= 1) return _currentSongIndex
                    var newIndex = _currentSongIndex
                    // Simple random loop to find a different index
                    while (newIndex == _currentSongIndex) {
                        newIndex = Random.nextInt(0, _currentPlaylist.value.size)
                    }
                    return newIndex
                } else {
                    val newIndex = _currentSongIndex + 1
                    return if (newIndex >= _currentPlaylist.value.size) {
                        // Loop back to start if Repeat All is on
                        if (repeatMode == RepeatMode.ALL) 0 else _currentSongIndex
                    } else {
                        newIndex
                    }
                }
            }
        }
    }

    /**
     * Helper to retrieve the actual Song object for "Next Up" display.
     */
    private fun getNextUpSong(forceNext: Boolean = false): Song? {
        if (_currentPlaylist.value.isEmpty()) return null
        val originalRepeatMode = repeatMode
        // Force calculation of next song even if Repeat One is on (for manual skips)
        if (forceNext && repeatMode == RepeatMode.ONE) repeatMode = RepeatMode.ALL
        val nextIndex = getNextIndex(peek = true)
        if (forceNext) repeatMode = originalRepeatMode
        return if (nextIndex != -1 && nextIndex != _currentSongIndex) _currentPlaylist.value.getOrNull(nextIndex) else null
    }

    fun playPreviousSong(context: Context) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return
        // Circular navigation for previous
        _currentSongIndex = if (_currentSongIndex - 1 < 0) playlist.size - 1 else _currentSongIndex - 1
        val previousSong = playlist[_currentSongIndex]
        val dominantColor = songColorCache[previousSong.id] ?: Color(0xFF444444)
        _playerUiState.value = _playerUiState.value.copy(currentSong = previousSong, nextUpSong = getNextUpSong(), isBuffering = true, dominantColor = dominantColor)
        prepareSongInternal(context, previousSong, _playerUiState.value.loadState)
    }

    /**
     * Handles logic for skipping to the next track.
     * Respects Shuffle, Repeat One (unless forceManual is true), and Repeat All settings.
     */
    fun playNextSong(context: Context, forceManual: Boolean = false) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        // If Repeat One is active and this isn't a user click, replay current song
        if (repeatMode == RepeatMode.ONE && !forceManual) {
            prepareSongInternal(context, playlist[_currentSongIndex], _playerUiState.value.loadState)
            return
        }

        val nextIndex: Int
        if (isShuffleEnabled) {
            if (playlist.size <= 1) return
            var newIndex = _currentSongIndex
            while (newIndex == _currentSongIndex) newIndex = Random.nextInt(0, playlist.size)
            nextIndex = newIndex
        } else {
            val newIndex = _currentSongIndex + 1
            nextIndex = if (newIndex >= playlist.size) {
                // End of playlist reached
                if (repeatMode == RepeatMode.ALL || (repeatMode == RepeatMode.ONE && forceManual)) 0
                else return // Stop playback if no repeat
            } else {
                newIndex
            }
        }

        _currentSongIndex = nextIndex
        val nextSong = playlist[_currentSongIndex]
        val dominantColor = songColorCache[nextSong.id] ?: Color(0xFF444444)

        _playerUiState.value = _playerUiState.value.copy(currentSong = nextSong, nextUpSong = getNextUpSong(), isBuffering = true, dominantColor = dominantColor)
        prepareSongInternal(context, nextSong, _playerUiState.value.loadState)
    }
}