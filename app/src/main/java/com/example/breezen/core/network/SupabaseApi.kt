package com.example.breezen.core.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import java.net.HttpURLConnection
import java.net.URL

// ------- DATA MODELS -------
data class Tab(val id: String, val name: String)

data class Category(
    val id: String,
    val tab_id: String,
    val name: String
)

data class Song(
    val id: String,
    val category_id: String,
    val title: String,
    val duration: Int,
    val artist: String,
    val is_favorite: Boolean,
    val stream_id: String,
    val dominant_color: String?,
    val got_error: Boolean = false,
    val got_error_desc: String? = null
)

data class ErrorReportBody(
    val got_error: Boolean = true,
    val got_error_desc: String
)

data class FeedbackBody(val feedback: String)


// ------- SUPABASE API -------
interface SupabaseAPI {

    @GET("tabs")
    suspend fun getTabs(
        @Header("apiKey") apiKey: String,
        @Header("Authorization") authorization: String
    ): List<Tab>

    @GET("categories")
    suspend fun getCategories(
        @Header("apiKey") apiKey: String,
        @Header("Authorization") authorization: String
    ): List<Category>

    // Only fetch songs where got_error = false
    @GET("items?got_error=is.false")
    suspend fun getSongs(
        @Header("apiKey") apiKey: String,
        @Header("Authorization") authorization: String
    ): List<Song>

//    @POST("mood_logs")
//    suspend fun setMoodValue(
//        @Header("apiKey") apiKey: String,
//        @Header("Authorization") authorization: String,
//    )

    // ------- Important: PATCH song by id to flag an error -------
    @PATCH("items")
    suspend fun reportError(
        @Header("apiKey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") id: String, // pass value like "eq.123"
        @Body body: ErrorReportBody
    )

    @POST("feedback")
    suspend fun sendFeedback(
        @Header("apiKey") apiKey: String,
        @Header("Authorization") auth: String,
        @Body body: FeedbackBody
    )
}


// ------- RETROFIT CLIENT -------
object RetroFitClient {
    val api: SupabaseAPI by lazy {
        Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseAPI::class.java)
    }
}


// ------- TELEGRAM HELPERS -------
suspend fun getMusicStreamUrl(botToken: String, fileID: String): String? {
    MusicCacheManager.getCachedStreamUrl(fileID)?.let { return it }

    return withContext(Dispatchers.IO) {
        try {
            val getFileUrl =
                "https://api.telegram.org/bot$botToken/getFile?file_id=$fileID"

            val connection = URL(getFileUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000

            val jsonData = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            val filePath = JSONObject(jsonData)
                .getJSONObject("result")
                .getString("file_path")

            val downloadUrl =
                "https://api.telegram.org/file/bot$botToken/$filePath"

            MusicCacheManager.putStreamUrl(fileID, downloadUrl)
            downloadUrl

        } catch (e: Exception) {
            Log.e("SupabaseStorage", "Failed to get music stream URL", e)
            null
        }
    }
}

//suspend fun retrieveMusicFile(
//    context: Context,
//    botToken: String,
//    fileID: String
//): String? {
//    return withContext(Dispatchers.IO) {
//        try {
//            val streamUrl = getMusicStreamUrl(botToken, fileID)
//                ?: return@withContext null
//
//            val fileName = "tg_${fileID}.opus"
//
//            MusicCacheManager.downloadIfMissing(context, streamUrl, fileName)
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//}
