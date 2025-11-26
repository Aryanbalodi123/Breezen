package com.example.breezen.core.network

// Use the native Keys object to obtain sensitive strings at runtime
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

const val TELEGRAM_CHANNEL_ID = -1002482311457



//package com.example.breezen.core.network
//
//// ------------------ SUPABASE ------------------
//const val SUPABASE_API_KEY_ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNmdWppc3ZlcnptamRiYnVwZGttIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg3MTY2MzQsImV4cCI6MjA2NDI5MjYzNH0.ShF7oxMFhS5CnhhGUPikgp8XhKJaGosZj-kCtGyEj3E"
//const val SUPABASE_URL = "https://cfujisverzmjdbbupdkm.supabase.co/rest/v1/"
//const val SUPABASE_URL_AUTH = "https://cfujisverzmjdbbupdkm.supabase.co"
//const val IMAGE_BUCKET_URL = "https://cfujisverzmjdbbupdkm.supabase.co/storage/v1/object/public/breezen/songs_image/"
//const val GUIDED_AUDIO_BUCKET_URL = "https://cfujisverzmjdbbupdkm.supabase.co/storage/v1/object/public/breezen/guided_meditation_audio/"
//// ------------------ TELEGRAM ------------------
//const val TELEGRAM_BOT_TOKEN = "7717323235:AAFlc9TTF9137Zq1X43KraruBQ2ZJhCNGr0"
//const val TELEGRAM_CHANNEL_ID = -1002482311457
//const val TELEGRAM_URL = "https://api.telegram.org/file/bot$TELEGRAM_BOT_TOKEN/"
//
//// ------------------ GEMINI ------------------
//const val GEMINI_API_KEY = "AIzaSyDFiuSlq_Xd1X2JxXNBLKJFzy4HOWmoR8I" // move to secure storage in prod