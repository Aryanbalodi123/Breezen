package com.example.breezen.feature.music

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breezen.core.network.Category
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
import kotlin.random.Random
import androidx.compose.runtime.mutableStateOf as mutableStateOf2

// ... (PlayerLoadState and PlayerUiState data classes are unchanged) ...
enum class PlayerLoadState { IDLE, INITIAL, TRANSITIONING }
data class PlayerUiState(
    val currentSong: Song? = null,
    val streamUrl: String = "",
    val loadState: PlayerLoadState = PlayerLoadState.INITIAL,
    val isBuffering: Boolean = false,
    val dominantColor: Color = Color(0xFF444444),
    val nextUpSong: Song? = null,
    val nextStreamUrl: String = ""
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

    // --- NEW: Color cache map for INSTANT loading ---
    val songColorCache = mutableStateMapOf<String, Color>()
    // --- END NEW ---

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

    // Pass context
    fun fetchSongData(context: Context) {
        viewModelScope.launch {
            try {
                val resultTab = RetroFitClient.api.getTabs(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                _tabs.value = resultTab

                val resultCategory = RetroFitClient.api.getCategories(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                _categories.value = resultCategory.groupBy { it.tab_id }

                val resultSong = RetroFitClient.api.getSongs(SUPABASE_API_KEY_ANON, "Bearer $SUPABASE_API_KEY_ANON")
                _songs.value = resultSong.groupBy { it.category_id }
                _allSongs.value = resultSong


            } catch (e: Exception) {
                Log.e("TabsViewModel", "Failed to fetch song data", e)
            }
        }
    }

//    // --- NEW: Helper to parse and cache colors ---
//    private fun populateColorCache(songs: List<Song>) {
//        val defaultColor = Color(0xFF444444)
//        for (song in songs) {
//            val color = try {
//                if (song.dominant_color != null) {
//                    Color(song.dominant_color.toColorInt())
//                } else {
//                    defaultColor
//                }
//            } catch (e: Exception) {
//                defaultColor
//            }
//            songColorCache[song.id] = color
//        }
//    }

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

        // --- FIX: Get color from the cache INSTANTLY ---
        val dominantColor = songColorCache[song.id] ?: Color(0xFF444444)

        _playerUiState.value = PlayerUiState(
            loadState = PlayerLoadState.INITIAL,
            dominantColor = dominantColor // Set it immediately
        )
        // --- END FIX ---

        prepareSongInternal(context, song, PlayerLoadState.INITIAL)
    }

    // --- DELETED: precacheDominantColor() function ---

    // ... (rest of functions are the same) ...

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        viewModelScope.launch {
            val nextSong = getNextUpSong()
            val nextStreamUrl = nextSong?.let {
                getMusicStreamUrl(
                    TELEGRAM_BOT_TOKEN,
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
                    TELEGRAM_BOT_TOKEN,
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

    private fun prepareSongInternal(context: Context, song: Song, triggeredBy: PlayerLoadState) {
        viewModelScope.launch {
            try {
                val streamUrlDeferred = async {
                    getMusicStreamUrl(
                        TELEGRAM_BOT_TOKEN,
                        song.stream_id
                    )
                }
                val nextSongDeferred = async { getNextUpSong() }

                val musicStreamUrl = streamUrlDeferred.await()
                val nextSong = nextSongDeferred.await()

                val localFile =
                    MusicCacheManager.getCachedFile(context, "tg_${song.stream_id}.opus")
                        .takeIf { it.exists() }?.absolutePath

                nextSong?.let { ns ->
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val cachedNextUrl = MusicCacheManager.getCachedStreamUrl(ns.stream_id)
                                ?: getMusicStreamUrl(
                                    TELEGRAM_BOT_TOKEN,
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

                val effectiveUrl = localFile ?: musicStreamUrl ?: ""

                val nextStreamUrl = nextSong?.let {
                    MusicCacheManager.getCachedStreamUrl(it.stream_id) ?: getMusicStreamUrl(
                        TELEGRAM_BOT_TOKEN,
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

    fun playPreviousSong(context: Context) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        _currentSongIndex =
            if (_currentSongIndex - 1 < 0) playlist.size - 1 else _currentSongIndex - 1

        val previousSong = playlist[_currentSongIndex]

        val newNextUpSong = getNextUpSong()

        // --- FIX: Get color from cache ---
        val dominantColor = songColorCache[previousSong.id] ?: Color(0xFF444444)

        _playerUiState.value = _playerUiState.value.copy(
            currentSong = previousSong,
            nextUpSong = newNextUpSong,
            isBuffering = true,
            dominantColor = dominantColor // Set it
        )

        prepareSongInternal(context, previousSong, _playerUiState.value.loadState)
    }

    fun playNextSong(context: Context, forceManual: Boolean = false) {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        if (repeatMode == RepeatMode.ONE && !forceManual) {
            Log.d("TabViewModel", "Repeating single song")
            val currentSong = playlist[_currentSongIndex]
            prepareSongInternal(context, currentSong, _playerUiState.value.loadState)
            return
        }

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
                if (repeatMode == RepeatMode.ALL || (repeatMode == RepeatMode.ONE && forceManual)) {
                    nextIndex = 0 // Wrap to start
                } else {
                    Log.d("TabViewModel", "End of playlist, stopping.")
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

        val nextUpSong = getNextUpSong()

        // --- FIX: Get color from cache ---
        val dominantColor = songColorCache[nextSong.id] ?: Color(0xFF444444)

        _playerUiState.value = _playerUiState.value.copy(
            currentSong = nextSong,
            nextUpSong = nextUpSong,
            isBuffering = true,
            dominantColor = dominantColor // Set it
        )

        prepareSongInternal(context, nextSong, _playerUiState.value.loadState)
    }
}