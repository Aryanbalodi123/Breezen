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
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


const val SUPABASE_API_KEY_ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNmdWppc3ZlcnptamRiYnVwZGttIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg3MTY2MzQsImV4cCI6MjA2NDI5MjYzNH0.ShF7oxMFhS5CnhhGUPikgp8XhKJaGosZj-kCtGyEj3E"
const val SUPABASE_URL = "https://cfujisverzmjdbbupdkm.supabase.co/rest/v1/"
const val SUPABASE_URL_AUTH = "https://cfujisverzmjdbbupdkm.supabase.co"
const val SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNmdWppc3ZlcnptamRiYnVwZGttIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0ODcxNjYzNCwiZXhwIjoyMDY0MjkyNjM0fQ.bFl0jGFlw6O4dyQg8FirUOxXGuCxHXSU02NfOhDIKEo"


const val TELEGRAM_BOT_TOKEN = "7717323235:AAFlc9TTF9137Zq1X43KraruBQ2ZJhCNGr0"
const val TELEGRAM_CHANNEL_ID = -1002482311457
const val TELEGRAM_URL ="https://api.telegram.org/file/bot$TELEGRAM_BOT_TOKEN/"
const val IMAGE_BUCKET_URL = "https://cfujisverzmjdbbupdkm.supabase.co/storage/v1/object/public/breezen/songs_image/"



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

suspend fun retrieveMusicFile(
    context: Context,
    botToken: String,
    fileID: String
): String? {
    return withContext(Dispatchers.IO) {
        try {
            // Step 1: Ask Telegram for the file path
            val getFileUrl = "https://api.telegram.org/bot$botToken/getFile?file_id=$fileID"
            val connection = URL(getFileUrl).openConnection() as HttpURLConnection
            val jsonData = connection.inputStream.bufferedReader().readText()
            Log.d("file path ask" , jsonData)

            connection.disconnect()

            val filePath = JSONObject(jsonData)
                .getJSONObject("result")
                .getString("file_path")

            // Step 2: Get file name from filePath (last part after '/')
            val fileName = filePath.substringAfterLast("/")
            Log.d("file name" , fileName)

            // Step 3: Prepare local file location
            val localFile = File(context.filesDir, fileName)
            if (localFile.exists()) {
                return@withContext localFile.absolutePath
            }
            // Step 4: Build direct download URL
            val downloadUrl = "https://api.telegram.org/file/bot$botToken/$filePath"

            // Step 5: Download and save locally
            val fileConnection = URL(downloadUrl).openConnection() as HttpURLConnection
            val inputStream = fileConnection.inputStream
            val outputStream = FileOutputStream(localFile)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            fileConnection.disconnect()
            Log.d("file path" , localFile.absolutePath)
            // Step 6: Return saved file path

            localFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
