package com.example.breezen.core.network

object Keys {
    init {
        System.loadLibrary("secrets")
    }

    // If you need to pass Context for any reason, add an init(context: Context) native call.
    external fun getGeminiKey(): String
    external fun getTelegramBotToken(): String
    external fun getSupabaseAnonKey(): String
}