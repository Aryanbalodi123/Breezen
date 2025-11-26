package com.example.breezen.core.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.net.HttpURLConnection
import java.net.URL

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
    val stream_id:String,
    // --- ADD THIS LINE ---
    val dominant_color: String? // This is your new database column
)

//data class DominantColorUpdate(val dominant_color: String)

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


    @POST("mood_logs")
    suspend fun setMoodValue(
        @Header("apiKey") apiKey : String,
        @Header("Authorization") authorization : String,
    )

//    @PATCH("items")
//    suspend fun updateSongColor(
//        @Header("apiKey") apiKey : String,
//        @Header("Authorization") authorization : String,
//        @Header("Prefer") prefer : String = "return=minimal",
//        @Header("Content-Type") contentType : String = "application/json",
//        @Header("Accept") accept : String = "application/json",
//        @Query("id") filter : String,
//        @Body body : DominantColorUpdate
//    ): Response<Unit>


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