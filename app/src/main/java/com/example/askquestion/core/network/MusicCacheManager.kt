package com.example.askquestion.core.network

import android.content.Context
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * In-memory LRU cache for mapping Telegram file_id -> stream URL
 * and simple disk cache for downloaded media files (cacheDir/music_cache).
 *
 * Simple, safe, and dependency-free. Use this for quick wins.
 */
object MusicCacheManager {
    // In-memory cache: file_id -> streamURL
    private val streamUrlCache = object : LruCache<String, String>(200) {}

    fun getCachedStreamUrl(fileId: String): String? = streamUrlCache.get(fileId)
    fun putStreamUrl(fileId: String, url: String) = streamUrlCache.put(fileId, url)

    private const val SUBDIR = "music_cache"

    private fun cacheDir(context: Context): File {
        val dir = File(context.cacheDir, SUBDIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Returns the File object for a cached filename (does not ensure it exists).
     */
    fun getCachedFile(context: Context, fileName: String): File =
        File(cacheDir(context), fileName)

    /**
     * Downloads the given URL into cacheDir/music_cache/fileName if missing.
     * Returns absolute path when successful, or null on failure.
     *
     * Implementation notes:
     * - Downloads to a .tmp file first, then renames to final name.
     * - network IO runs on Dispatchers.IO.
     */
    suspend fun downloadIfMissing(context: Context, url: String, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val localFile = getCachedFile(context, fileName)
                if (localFile.exists()) return@withContext localFile.absolutePath

                // download to temp file first
                val temp = File(localFile.parentFile, "$fileName.tmp")
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.instanceFollowRedirects = true

                connection.inputStream.use { input ->
                    FileOutputStream(temp).use { out ->
                        input.copyTo(out)
                    }
                }
                connection.disconnect()

                // move temp -> final
                if (!temp.renameTo(localFile)) {
                    temp.copyTo(localFile, overwrite = true)
                    temp.delete()
                }
                localFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}