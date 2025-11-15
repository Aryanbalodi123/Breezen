package com.example.askquestion.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import java.net.HttpURLConnection
import java.net.URL

// ------------------ CONFIG ------------------
const val SUPABASE_API_KEY_ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNmdWppc3ZlcnptamRiYnVwZGttIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg3MTY2MzQsImV4cCI6MjA2NDI5MjYzNH0.ShF7oxMFhS5CnhhGUPikgp8XhKJaGosZj-kCtGyEj3E"
const val SUPABASE_URL = "https://cfujisverzmjdbbupdkm.supabase.co/rest/v1/"
const val SUPABASE_URL_AUTH = "https://cfujisverzmjdbbupdkm.supabase.co"
const val SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNmdWppc3ZlcnptamRiYnVwZGttIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0ODcxNjYzNCwiZXhwIjoyMDY0MjkyNjM0fQ.bFl0jGFlw6O4dyQg8FirUOxXGuCxHXSU02NfOhDIKEo"

const val TELEGRAM_BOT_TOKEN = "7717323235:AAFlc9TTF9137Zq1X43KraruBQ2ZJhCNGr0"
const val TELEGRAM_CHANNEL_ID = -1002482311457
const val TELEGRAM_URL = "https://api.telegram.org/file/bot$TELEGRAM_BOT_TOKEN/"
const val IMAGE_BUCKET_URL = "https://cfujisverzmjdbbupdkm.supabase.co/storage/v1/object/public/breezen/songs_image/"

// ------------------ DATA ------------------
data class Tab(
    val id : String,
    val name: String
)
data class Category(
    val id : String,
    val tab_id: String,
    val name: String
)

data class Song(
    val id : String,
    val category_id: String,
    val title: String,
    val duration:Int,
    val artist :String,
    val is_favorite: Boolean,
    val image_url:String,
    val stream_id:String
)

// ------------------ RETROFIT API ------------------
interface SupabaseAPI {
    @GET("tabs")
    suspend fun getTabs(
        @Header("apiKey") apiKey :String,
        @Header("Authorization") authorization :String
    ): List<Tab>

    @GET("categories")
    suspend fun getCategories(
        @Header("apiKey") apiKey :String,
        @Header("Authorization") authorization :String
    ): List<Category>

    @GET("items")
    suspend fun getSongs(
        @Header("apiKey") apiKey :String,
        @Header("Authorization") authorization :String
    ): List<Song>
}

object RetroFitClient {
    val api: SupabaseAPI by lazy {
        Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseAPI::class.java)
    }
}

// ------------------ TELEGRAM HELPERS ------------------

/**
 * 🔥 PERFORMANCE FIX: Gets the streamable URL from Telegram with an in-memory LRU cache.
 * This avoids asking /getFile repeatedly for the same file_id.
 */
suspend fun getMusicStreamUrl(botToken: String, fileID: String): String? {
    // Fast path: return cached URL if present
    MusicCacheManager.getCachedStreamUrl(fileID)?.let { return it }

    return withContext(Dispatchers.IO) {
        try {
            val getFileUrl = "https://api.telegram.org/bot$botToken/getFile?file_id=$fileID"
            val connection = URL(getFileUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            val jsonData = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            Log.d("SupabaseStorage", "getFile response: $jsonData")

            val filePath = JSONObject(jsonData)
                .getJSONObject("result")
                .getString("file_path")

            val downloadUrl = "https://api.telegram.org/file/bot$botToken/$filePath"
            // cache it
            MusicCacheManager.putStreamUrl(fileID, downloadUrl)
            downloadUrl
        } catch (e: Exception) {
            Log.e("SupabaseStorage", "Failed to get music stream URL", e)
            e.printStackTrace()
            null
        }
    }
}

/**
 * Downloads the full file (DEPRECATED for immediate streaming), but now backed by MusicCacheManager.
 * Returns local file absolute path when the file is present or successfully downloaded, else null.
 */
suspend fun retrieveMusicFile(
    context: Context,
    botToken: String,
    fileID: String
): String? {
    return withContext(Dispatchers.IO) {
        try {
            // Get stream URL (possibly cached)
            val streamUrl = getMusicStreamUrl(botToken, fileID) ?: return@withContext null

            // Deterministic cache filename
            val fileName = "tg_${fileID}.opus"

            // Use MusicCacheManager to download if missing
            val localPath = MusicCacheManager.downloadIfMissing(context, streamUrl, fileName)
            localPath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
