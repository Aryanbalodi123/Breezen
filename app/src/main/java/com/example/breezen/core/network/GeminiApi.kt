package com.example.breezen.core.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// ----------------- REQUEST MODELS -----------------
data class GeminiRequest(val contents: List<RequestContent>)
data class RequestContent(val parts: List<RequestPart>)
data class RequestPart(val text: String)

// ----------------- RESPONSE MODELS -----------------
data class GeminiResponse(val candidates: List<Candidate>?)
data class Candidate(val content: ResponseContent?)
data class ResponseContent(val parts: List<ResponsePart>?)
data class ResponsePart(val text: String?)

// ----------------- API SERVICE -----------------
interface GeminiAPI {
    // Note: remove the @Query apiKey param — interceptor will append it
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(@Body request: GeminiRequest): GeminiResponse
}

// ----------------- RETROFIT INSTANCE -----------------
object GeminiService {

    private val logging = HttpLoggingInterceptor().apply {
        // Use BODY for debugging — remove or change to BASIC in prod
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor to add the API key as a query parameter to every request
    private val apiKeyInterceptor = Interceptor { chain ->
        val original = chain.request()
        val originalUrl = original.url
        val urlWithKey = originalUrl.newBuilder()
            .addQueryParameter("key", GEMINI_API_KEY)
            .build()

        val reqWithKey: Request = original.newBuilder()
            .url(urlWithKey)
            .build()
        chain.proceed(reqWithKey)
    }

    // Configure OkHttpClient with longer timeouts and logging
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS) // overall time for the call
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GeminiAPI = retrofit.create(GeminiAPI::class.java)
}