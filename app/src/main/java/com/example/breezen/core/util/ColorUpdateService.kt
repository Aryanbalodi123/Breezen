//package com.example.breezen.core.util
//
//import android.content.Context
//import android.util.Log
//import androidx.compose.ui.graphics.Color
//import androidx.core.graphics.drawable.toBitmap
//import androidx.palette.graphics.Palette
//import coil.imageLoader
//import coil.request.ImageRequest
//import coil.request.SuccessResult
//import com.example.breezen.core.network.IMAGE_BUCKET_URL
//import com.example.breezen.core.network.RetroFitClient
//import com.example.breezen.core.network.SUPABASE_API_KEY_ANON
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//object ColorUpdateService {
//
//    private const val TAG = "ColorUpdateService"
//
//    suspend fun runColorUpdate(context: Context) {
//        withContext(Dispatchers.IO) {
//            Log.d(TAG, "Starting color update service...")
//
//            val songs = try {
//                RetroFitClient.api.getSongs(
//                    SUPABASE_API_KEY_ANON,
//                    "Bearer $SUPABASE_API_KEY_ANON"
//                )
//            } catch (e: Exception) {
//                Log.e(TAG, "❌ Failed to fetch songs", e)
//                return@withContext
//            }
//
//            val songsToUpdate = songs.filter { it.dominant_color == null }
//            Log.d(TAG, "🎨 Need to update ${songsToUpdate.size} songs")
//
//            for (song in songsToUpdate) {
//                try {
//                    val imageUrl = IMAGE_BUCKET_URL + song.id + ".webp"
//
//                    Log.d(TAG, "🖼 Loading image: $imageUrl")
//
//                    val request = ImageRequest.Builder(context)
//                        .data(imageUrl)
//                        .allowHardware(false)
//                        .build()
//
//                    val result = (context.imageLoader.execute(request) as? SuccessResult)?.drawable
//                    val bitmap = result?.toBitmap()
//
//                    if (bitmap == null) {
//                        Log.w(TAG, "⚠ Could not load bitmap for ${song.id}")
//                        continue
//                    }
//
//                    val palette = Palette.from(bitmap).generate()
//                    val colorInt = findGoodColor(palette)
//                    val colorHex = String.format("#%06X", 0xFFFFFF and colorInt)
//
//                    val response = RetroFitClient.api.updateSongColor(
//                        apiKey = SUPABASE_API_KEY_ANON,
//                        authorization = "Bearer $SUPABASE_API_KEY_ANON",
//                        filter = "eq.${song.id}",
//                        body = DominantColorUpdate(dominant_color = colorHex)
//                    )
//
//                    if (response.isSuccessful) {
//                        Log.d(TAG, "✅ DB UPDATED: ${song.id} → $colorHex")
//                    } else {
//                        Log.e(TAG, "❌ DB UPDATE FAILED for ${song.id}: ${response.errorBody()?.string()}")
//                    }
//
//                } catch (e: Exception) {
//                    Log.e(TAG, "❌ Error processing ${song.id}", e)
//                }
//            }
//
//            Log.d(TAG, "🎉 Color update service finished.")
//        }
//    }
//
//    private fun findGoodColor(palette: Palette): Int {
//        val defaultColor = 0xFF444444.toInt()
//
//        val vibrant = palette.getVibrantColor(0)
//        if (vibrant != 0 && !isBad(vibrant)) return vibrant
//
//        val muted = palette.getMutedColor(0)
//        if (muted != 0 && !isBad(muted)) return muted
//
//        val dominant = palette.getDominantColor(0)
//        if (dominant != 0 && !isBad(dominant)) return dominant
//
//        val dark = palette.getDarkVibrantColor(0)
//        if (dark != 0 && !isBad(dark)) return dark
//
//        val light = palette.getLightMutedColor(0)
//        if (light != 0 && !isBad(light)) return light
//
//        return defaultColor
//    }
//
//    private fun isBad(color: Int): Boolean {
//        val c = Color(color)
//        return (c.red < 0.1f && c.green < 0.1f && c.blue < 0.1f) || // black-ish
//                (c.red > 0.9f && c.green > 0.9f && c.blue > 0.9f)    // white-ish
//    }
//}
