package com.example.breezen.core.network

val GEMINI_API_KEY: String
    get() = Keys.getGeminiKey()

val TELEGRAM_BOT_TOKEN: String
    get() = Keys.getTelegramBotToken()

val SUPABASE_API_KEY_ANON: String
    get() = Keys.getSupabaseAnonKey()

const val SUPABASE_URL = "https://cfujisverzmjdbbupdkm.supabase.co/rest/v1/"
const val SUPABASE_URL_AUTH = "https://cfujisverzmjdbbupdkm.supabase.co"
const val IMAGE_BUCKET_URL = "https://cfujisverzmjdbbupdkm.supabase.co/storage/v1/object/public/breezen/songs_image/"
const val GUIDED_AUDIO_BUCKET_URL = "https://cfujisverzmjdbbupdkm.supabase.co/storage/v1/object/public/breezen/guided_meditation_audio/"


