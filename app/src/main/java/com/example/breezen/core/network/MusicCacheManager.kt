package com.example.breezen.core.network

import android.content.Context
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

// ------- MUSIC CACHE MANAGER -------
// ------- Handles stream URL cache + disk file caching -------
object MusicCacheManager {

    // In-memory cache: file_id -> streamURL
    private val streamUrlCache = object : LruCache<String, String>(200) {}

    fun getCachedStreamUrl(fileId: String): String? = streamUrlCache.get(fileId)
    fun putStreamUrl(fileId: String, url: String) {
        streamUrlCache.put(fileId, url)
    }

    private const val SUBDIR = "music_cache"

    private fun cacheDir(context: Context?): File {
        val dir = File(context?.cacheDir, SUBDIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getCachedFile(context: Context?, fileName: String): File =
        File(cacheDir(context), fileName)

    // ------- Important: Downloads file only if missing -------
    // Uses temp file first → prevents corrupted partial files
    suspend fun downloadIfMissing(context: Context?, url: String, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val localFile = getCachedFile(context, fileName)
                if (localFile.exists()) return@withContext localFile.absolutePath

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

                // Rename temp → final safely
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
