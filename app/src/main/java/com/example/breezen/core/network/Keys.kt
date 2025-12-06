package com.example.breezen.core.network

import android.content.Context

object Keys {
    init {
        System.loadLibrary("core_runtime")
    }

    // Pass context to ALL of them now so C++ can check the signature
    external fun getGeminiKey(context: Context): String
    external fun getTelegramBotToken(context: Context): String
    external fun getSupabaseAnonKey(context: Context): String
}