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

enum class PlayerLoadState { IDLE, INITIAL, TRANSITIONING }
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


class TabViewModel : ViewModel() {

    enum class RepeatMode { OFF, ALL, ONE }

    private val _playerUiState = mutableStateOf(PlayerUiState())
    private val _tabs = mutableStateOf<List<Tab>>(emptyList())
    private val _categories = mutableStateOf<Map<String, List<Category>>>(emptyMap())
    private val _songs = mutableStateOf<Map<String, List<Song>>>(emptyMap())
    private val _allSongs = mutableStateOf<List<Song>>(emptyList())
    private val _currentPlaylist = mutableStateOf<List<Song>>(emptyList())
    private var _currentSongIndex = -1

    val songColorCache = mutableStateMapOf<String, Color>()

    val playerUiState: State<PlayerUiState> = _playerUiState
    val tabs: State<List<Tab>> = _tabs
    val categories: State<Map<String, List<Category>>> = _categories
    val songs: State<Map<String, List<Song>>> = _songs
    val allSongs: State<List<Song>> = _allSongs

    var headerSong: Song? by mutableStateOf2(null)
    var featuredSongs: List<Song> by mutableStateOf2(emptyList())

    var isPlaying by mutableStateOf2(false)
    var isShuffleEnabled by mutableStateOf2(false)
    var repeatMode: RepeatMode by mutableStateOf2(RepeatMode.OFF)

    fun fetchSongData(context: Context) {
        viewModelScope.launch {
            try {
                val resultTab = RetroFitClient.api.getTabs(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                _tabs.value = resultTab

                val resultCategory = RetroFitClient.api.getCategories(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                _categories.value = resultCategory.groupBy { it.tab_id }

                // API already filters got_error=false via query, but safety filter here too
                val resultSong = RetroFitClient.api.getSongs(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                    .filter { !it.got_error }

                _songs.value = resultSong.groupBy { it.category_id }
                _allSongs.value = resultSong

            } catch (e: Exception) {
                Log.e("TabsViewModel", "Failed to fetch song data", e)
            }
        }
    }

    fun getDominantColor(song: Song?): Color {
        val raw = song?.dominant_color ?: return Color(0xFF444444)
        return try {
            Color(android.graphics.Color.parseColor("#$raw"))
        } catch (e: Exception) {
            Color(0xFF444444)
        }
    }

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

    // ... (toggleShuffle, toggleRepeat, onPlayerReadyAndImageLoaded, setIsBuffering omitted for brevity, identical to prev) ...
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

    private fun prepareSongInternal(context: Context, song: Song, triggeredBy: PlayerLoadState) {
        viewModelScope.launch {
            try {
                // 1. Try to get Stream URL
                val streamUrlDeferred = async { getMusicStreamUrl(TELEGRAM_BOT_TOKEN, song.stream_id) }
                val nextSongDeferred = async { getNextUpSong() }

                val musicStreamUrl = streamUrlDeferred.await()
                val nextSong = nextSongDeferred.await()

                // Check for failure
                if (musicStreamUrl.isNullOrEmpty()) {
                    throw Exception("Stream URL is null or empty")
                }

                val localFile = MusicCacheManager.getCachedFile(context, "tg_${song.stream_id}.opus")
                    .takeIf { it.exists() }?.absolutePath
                val effectiveUrl = localFile ?: musicStreamUrl

                // Pre-cache next song
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

                // 1. Report to DB
                reportPlaybackError(song, e.message ?: "Unknown playback error")

                // 2. Remove from local list so we don't pick it again immediately
                val filteredList = _currentPlaylist.value.filter { it.id != song.id }
                _currentPlaylist.value = filteredList

                // 3. Notify UI (Toast)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error playing ${song.title}. Removing from playlist.", Toast.LENGTH_SHORT).show()
                }

                // 4. Try Next Song or Stop
                if (filteredList.isNotEmpty()) {
                    playNextSong(context, forceManual = true)
                } else {
                    // Playlist dead, exit player
                    // In a real app, you might want to navigateBack via an event channel
                    _playerUiState.value = _playerUiState.value.copy(loadState = PlayerLoadState.IDLE, currentSong = null)
                }
            }
        }
    }

    // ... (getNextIndex, getNextUpSong, playPreviousSong, playNextSong same as before) ...
    private fun getNextIndex(peek: Boolean = false): Int {
        if (_currentPlaylist.value.isEmpty()) return -1
        when (repeatMode) {
            RepeatMode.ONE -> return _currentSongIndex
            RepeatMode.ALL, RepeatMode.OFF -> {
                if (isShuffleEnabled) {
                    if (_currentPlaylist.value.size <= 1) return _currentSongIndex
                    var newIndex = _currentSongIndex
                    while (newIndex == _currentSongIndex) {
                        newIndex = Random.nextInt(0, _currentPlaylist.value.size)
                    }
                    return newIndex
                } else {
                    val newIndex = _currentSongIndex + 1
                    return if (newIndex >= _currentPlaylist.value.size) {
                        if (repeatMode == RepeatMode.ALL) 0 else _currentSongIndex
                    } else {
                        newIndex
                    }
                }
            }
        }
    }

    private fun getNextUpSong(forceNext: Boolean = false): Song? {
        if (_currentPlaylist.value.isEmpty()) return null
        val originalRepeatMode = repeatMode
        if (forceNext && repeatMode == RepeatMode.ONE) repeatMode = RepeatMode.ALL
        val nextIndex = getNextIndex(peek = true)
        if (forceNext) repeatMode = originalRepeatMode
        return if (nextIndex != -1 && nextIndex != _currentSongIndex) _currentPlaylist.value.getOrNull(nextIndex) else null
    }

    fun playPreviousSong(context: Context) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return
        _currentSongIndex = if (_currentSongIndex - 1 < 0) playlist.size - 1 else _currentSongIndex - 1
        val previousSong = playlist[_currentSongIndex]
        val dominantColor = songColorCache[previousSong.id] ?: Color(0xFF444444)
        _playerUiState.value = _playerUiState.value.copy(currentSong = previousSong, nextUpSong = getNextUpSong(), isBuffering = true, dominantColor = dominantColor)
        prepareSongInternal(context, previousSong, _playerUiState.value.loadState)
    }

    fun playNextSong(context: Context, forceManual: Boolean = false) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return
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
            if (newIndex >= playlist.size) {
                if (repeatMode == RepeatMode.ALL || (repeatMode == RepeatMode.ONE && forceManual)) nextIndex = 0
                else return
            } else {
                nextIndex = newIndex
            }
        }
        _currentSongIndex = nextIndex
        val nextSong = playlist[_currentSongIndex]
        val dominantColor = songColorCache[nextSong.id] ?: Color(0xFF444444)
        _playerUiState.value = _playerUiState.value.copy(currentSong = nextSong, nextUpSong = getNextUpSong(), isBuffering = true, dominantColor = dominantColor)
        prepareSongInternal(context, nextSong, _playerUiState.value.loadState)
    }
}