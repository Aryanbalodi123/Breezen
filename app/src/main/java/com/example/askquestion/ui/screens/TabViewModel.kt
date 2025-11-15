package com.example.askquestion.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.example.askquestion.network.Category
import com.example.askquestion.network.MusicCacheManager
import com.example.askquestion.network.Song
import com.example.askquestion.network.Tab
import com.example.askquestion.network.getMusicStreamUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.runtime.mutableStateOf as mutableStateOf2

enum class PlayerLoadState {
    IDLE,
    INITIAL,
    TRANSITIONING
}

data class PlayerUiState(
    val currentSong: Song? = null,
    val streamUrl: String = "",
    val loadState: PlayerLoadState = PlayerLoadState.INITIAL,
    val isBuffering: Boolean = false,
    val dominantColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(
        0xFF444444
    ),
    val nextUpSong: Song? = null,
    val nextStreamUrl: String = ""
)

class TabViewModel : ViewModel() {

    enum class RepeatMode {
        OFF, ALL, ONE
    }

    private val _playerUiState = mutableStateOf(PlayerUiState())
    private val _tabs = mutableStateOf<List<Tab>>(emptyList())
    private val _categories = mutableStateOf<Map<String, List<Category>>>(emptyMap())
    private val _songs = mutableStateOf<Map<String, List<Song>>>(emptyMap())
    private val _allSongs = mutableStateOf<List<Song>>(emptyList())
    private val _currentPlaylist = mutableStateOf<List<Song>>(emptyList())
    private var _currentSongIndex = -1

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

    fun fetchSongData() {
        viewModelScope.launch {
            try {
                // keep your existing retrofit calls in place (caller already had them)
                val resultTab = com.example.askquestion.network.RetroFitClient.api.getTabs(
                    apiKey = com.example.askquestion.network.SUPABASE_API_KEY_ANON,
                    authorization = "Bearer ${com.example.askquestion.network.SUPABASE_API_KEY_ANON}"
                )
                _tabs.value = resultTab

                val resultCategory =
                    com.example.askquestion.network.RetroFitClient.api.getCategories(
                        apiKey = com.example.askquestion.network.SUPABASE_API_KEY_ANON,
                        authorization = "Bearer ${com.example.askquestion.network.SUPABASE_API_KEY_ANON}"
                    )
                _categories.value = resultCategory.groupBy { it.tab_id }

                val resultSong = com.example.askquestion.network.RetroFitClient.api.getSongs(
                    apiKey = com.example.askquestion.network.SUPABASE_API_KEY_ANON,
                    authorization = "Bearer ${com.example.askquestion.network.SUPABASE_API_KEY_ANON}"
                )
                _songs.value = resultSong.groupBy { it.category_id }
                _allSongs.value = resultSong

            } catch (e: Exception) {
                Log.e("TabsViewModel", "Failed to fetch song data", e)
            }
        }
    }

    /**
     * Main entry point for playing a song.
     * Does not navigate — caller (UI) still navigates immediately.
     */
    fun playSong(context: Context, song: Song, playlist: List<Song>) {
        _currentPlaylist.value = playlist
        _currentSongIndex = playlist.indexOf(song)
        _playerUiState.value = PlayerUiState(loadState = PlayerLoadState.INITIAL)
        // Pre-cache dominant color in parallel (caller already had precache, keep it)
        precacheDominantColor(context, song)
        // Prepare stream URLs and next song info
        prepareSongInternal(context, song, PlayerLoadState.INITIAL)
    }

    // 🔥 **FIX**: Removed the buggy `syncStateToPlayerTransition` function.

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        viewModelScope.launch {
            val nextSong = getNextUpSong()
            val nextStreamUrl = nextSong?.let {
                getMusicStreamUrl(
                    com.example.askquestion.network.TELEGRAM_BOT_TOKEN,
                    it.stream_id
                )
            } ?: ""
            _playerUiState.value = _playerUiState.value.copy(
                nextUpSong = nextSong,
                nextStreamUrl = nextStreamUrl
            )
        }
    }

    fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        viewModelScope.launch {
            val nextSong = getNextUpSong()
            val nextStreamUrl = nextSong?.let {
                getMusicStreamUrl(
                    com.example.askquestion.network.TELEGRAM_BOT_TOKEN,
                    it.stream_id
                )
            } ?: ""
            _playerUiState.value = _playerUiState.value.copy(
                nextUpSong = nextSong,
                nextStreamUrl = nextStreamUrl
            )
        }
    }

    fun onPlayerReadyAndImageLoaded() {
        _playerUiState.value = _playerUiState.value.copy(loadState = PlayerLoadState.IDLE)
    }

    fun setIsBuffering(isBuffering: Boolean) {
        _playerUiState.value = _playerUiState.value.copy(isBuffering = isBuffering)
    }

    private fun precacheDominantColor(context: Context, song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = coil.request.ImageRequest.Builder(context)
                    .data(com.example.askquestion.network.IMAGE_BUCKET_URL + song.id + ".webp")
                    .allowHardware(false)
                    .build()
                val result =
                    (context.imageLoader.execute(request) as? coil.request.SuccessResult)?.drawable
                val color = result?.let { drawable ->
                    androidx.palette.graphics.Palette.from(drawable.toBitmap()).generate()
                        ?.getDominantColor(0xFF444444.toInt())
                        ?.let { androidx.compose.ui.graphics.Color(it) }
                } ?: androidx.compose.ui.graphics.Color(0xFF444444)

                _playerUiState.value = _playerUiState.value.copy(dominantColor = color)
            } catch (e: Exception) {
                Log.e("TabViewModel", "Dominant color precache failed", e)
                _playerUiState.value = _playerUiState.value.copy(
                    dominantColor = androidx.compose.ui.graphics.Color(0xFF444444)
                )
            }
        }
    }

    /**
     * 🔥 UPGRADE: Fetches URLs for *both* current and next songs in parallel.
     * Prefers local cached file if already downloaded; also starts background
     * prefetch for the next song (disk cache).
     */
    private fun prepareSongInternal(context: Context, song: Song, triggeredBy: PlayerLoadState) {
        viewModelScope.launch {
            try {
                // Get stream URL (this will use the in-memory cache if present)
                val streamUrlDeferred = async {
                    getMusicStreamUrl(
                        com.example.askquestion.network.TELEGRAM_BOT_TOKEN,
                        song.stream_id
                    )
                }
                val nextSongDeferred = async { getNextUpSong() }

                val musicStreamUrl = streamUrlDeferred.await()
                val nextSong = nextSongDeferred.await()

                // Attempt to find a local cached file for current song (non-blocking check)
                val localFile =
                    MusicCacheManager.getCachedFile(context, "tg_${song.stream_id}.opus")
                        .takeIf { it.exists() }?.absolutePath

                // Prefetch next song in background (disk cache)
                nextSong?.let { ns ->
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val cachedNextUrl = MusicCacheManager.getCachedStreamUrl(ns.stream_id)
                                ?: getMusicStreamUrl(
                                    com.example.askquestion.network.TELEGRAM_BOT_TOKEN,
                                    ns.stream_id
                                )
                            if (!cachedNextUrl.isNullOrEmpty()) {
                                MusicCacheManager.downloadIfMissing(
                                    context,
                                    cachedNextUrl,
                                    "tg_${ns.stream_id}.opus"
                                )
                            }
                        } catch (ignored: Exception) {
                        }
                    }
                }

                // Decide effective URL: prefer local file path if available (fast), otherwise remote stream URL
                val effectiveUrl = localFile ?: musicStreamUrl ?: ""

                val nextStreamUrl = nextSong?.let {
                    MusicCacheManager.getCachedStreamUrl(it.stream_id) ?: getMusicStreamUrl(
                        com.example.askquestion.network.TELEGRAM_BOT_TOKEN,
                        it.stream_id
                    )
                } ?: ""

                if (effectiveUrl.isNotEmpty()) {
                    _playerUiState.value = _playerUiState.value.copy(
                        currentSong = song,
                        streamUrl = effectiveUrl,
                        loadState = triggeredBy,
                        isBuffering = true,
                        nextUpSong = nextSong,
                        nextStreamUrl = nextStreamUrl
                    )
                } else {
                    Log.e("TabViewModel", "Failed to retrieve stream URL for ${song.title}")
                }
            } catch (e: Exception) {
                Log.e("TabViewModel", "Error in prepareSongInternal", e)
            }
        }
    }

    private fun getNextIndex(peek: Boolean = false): Int {
        if (_currentPlaylist.value.isEmpty()) return -1

        when (repeatMode) {
            RepeatMode.ONE -> {
                return _currentSongIndex
            }

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
        if (forceNext && repeatMode == RepeatMode.ONE) {
            repeatMode = RepeatMode.ALL
        }

        val nextIndex = getNextIndex(peek = true)

        if (forceNext) {
            repeatMode = originalRepeatMode
        }

        return if (nextIndex != -1 && nextIndex != _currentSongIndex) {
            _currentPlaylist.value.getOrNull(nextIndex)
        } else {
            null
        }
    }

    // 🔥 **FIX**: Replaced with robust `playPreviousSong`
    fun playPreviousSong(context: Context) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        // Logic: "Previous" button just goes back one in the list,
        // regardless of shuffle or repeat. Wraps at beginning.
        _currentSongIndex =
            if (_currentSongIndex - 1 < 0) playlist.size - 1 else _currentSongIndex - 1

        val previousSong = playlist[_currentSongIndex]

        // Get the 'next' song relative to the *new* current index
        val newNextUpSong = getNextUpSong()

        _playerUiState.value = _playerUiState.value.copy(
            currentSong = previousSong,
            nextUpSong = newNextUpSong,
            // loadState = PlayerLoadState.TRANSITIONING, // <-- REMOVED
            isBuffering = true
        )

        // Pass the *current* load state (likely IDLE)
        prepareSongInternal(context, previousSong, _playerUiState.value.loadState)
    }


    // 🔥 **FIX**: Replaced with robust `playNextSong`
    fun playNextSong(context: Context, forceManual: Boolean = false) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        // Special case: RepeatMode.ONE and this is an AUTO-transition
        if (repeatMode == RepeatMode.ONE && !forceManual) {
            Log.d("TabViewModel", "Repeating single song")
            // Just restart the current song
            val currentSong = playlist[_currentSongIndex]
            _playerUiState.value = _playerUiState.value.copy(
                // loadState = PlayerLoadState.TRANSITIONING // <-- REMOVED
            )
            // Pass the *current* load state (likely IDLE)
            prepareSongInternal(context, currentSong, _playerUiState.value.loadState)
            return
        }

        // All other cases (manual next, repeat all, repeat off, shuffle)
        // get the next index, bypassing repeat-one logic.
        val nextIndex: Int
        if (isShuffleEnabled) {
            if (playlist.size <= 1) return // No next song
            var newIndex = _currentSongIndex
            while (newIndex == _currentSongIndex) {
                newIndex = Random.nextInt(0, playlist.size)
            }
            nextIndex = newIndex
        } else {
            val newIndex = _currentSongIndex + 1
            if (newIndex >= playlist.size) {
                // Reached end of list
                if (repeatMode == RepeatMode.ALL || (repeatMode == RepeatMode.ONE && forceManual)) {
                    nextIndex = 0 // Wrap to start
                } else {
                    Log.d("TabViewModel", "End of playlist, stopping.")
                    // Don't change state, just let player stop.
                    return
                }
            } else {
                nextIndex = newIndex
            }
        }

        if (nextIndex == -1) {
            Log.d("TabViewModel", "End of playlist, stopping.")
            return
        }

        _currentSongIndex = nextIndex
        val nextSong = playlist[_currentSongIndex]

        // Get the 'next-next' song for the UI bar
        val nextUpSong = getNextUpSong()

        _playerUiState.value = _playerUiState.value.copy(
            currentSong = nextSong,
            nextUpSong = nextUpSong,
            // loadState = PlayerLoadState.TRANSITIONING, // <-- REMOVED
            isBuffering = true
        )

        // Pass the *current* load state (likely IDLE)
        prepareSongInternal(context, nextSong, _playerUiState.value.loadState)
    }
}