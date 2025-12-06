package com.example.breezen.core.network

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object SecureEnv {

    private const val TAG = "SecureEnv"

    // Your release keystore SHA-256 (matches your keytool output)
    private const val ALLOWED_RELEASE_SHA256 =
        "F6:EF:7F:25:CA:8E:3B:80:75:8A:2A:A4:16:4D:7D:60:10:4A:7D:F2:04:D9:2B:90:0B:3E:9D:48:ED:8E:F1:8C"

    private lateinit var appContext: Context

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        initialized = true
    }

    // --------- SIGNATURE CHECK ---------
    private fun isSignatureValid(): Boolean {
        if (!initialized) {
            Log.e(TAG, "SecureEnv.init(context) not called yet")
            return false
        }

        // NOTE: We removed the "if (BuildConfig.DEBUG) return false" check.
        // Because we are signing the Debug build with the Release key in Gradle,
        // we WANT this check to run and verify the hash.

        return try {
            val pm = appContext.packageManager
            val pkgInfo = pm.getPackageInfo(
                appContext.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )

            val signatures = pkgInfo.signingInfo?.apkContentsSigners
            if (signatures.isNullOrEmpty()) {
                Log.e(TAG, "No signatures found on app")
                return false
            }

            val md = java.security.MessageDigest.getInstance("SHA-256")

            signatures.forEach { sig ->
                val hash = md.digest(sig.toByteArray())
                val hex = hash.joinToString(":") { "%02X".format(it) }

                if (hex.equals(ALLOWED_RELEASE_SHA256, ignoreCase = true)) {
                    // Success! This is either the Release build OR your local Debug build.
                    return true
                }
            }

            Log.e(TAG, "Signature mismatch – returning empty keys.")
            false

        } catch (e: Exception) {
            Log.e(TAG, "Signature check failed: ${e.message}")
            false
        }
    }

    // --------- PUBLIC SAFE ACCESSORS ---------





        val geminiApiKey: String
            get() {
                val context = appContext
                return Keys.getGeminiKey(context)
            }

        val telegramBotToken: String
            get() {
                val context = appContext
                return Keys.getTelegramBotToken(context)
            }

        val supabaseAnonKey: String
            get() {
                val context = appContext ?: throw IllegalStateException("SecureEnv.init(context) must be called in Application.onCreate()")
                return Keys.getSupabaseAnonKey(context)
            }
    }

