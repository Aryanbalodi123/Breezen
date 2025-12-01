package com.example.breezen.core.data


import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.time.LocalDate

val Context.dataStore by preferencesDataStore("app_settings")



object MoodPreference {
    private val MOOD_BOOLEAN = booleanPreferencesKey("mood_boolean")
    private val MOOD_SET_DATE = stringPreferencesKey("mood_set_date")

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun setMoodBoolean(context :Context, value: Boolean){
        context.dataStore.edit { prefs ->
            prefs[MOOD_BOOLEAN] = value
        prefs[MOOD_SET_DATE] = LocalDate.now().toString()
        }}

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMoodBoolean(context: Context) =
        context.dataStore.data.map { prefs ->
            val today = LocalDate.now().toString()
            val savedDate = prefs[MOOD_SET_DATE]

            if (savedDate == today) {
                prefs[MOOD_BOOLEAN] ?: false
            } else {
                false
            }
        }
}


// In DataStoreManager.kt

object UserPreferences {
    private val USERNAME_KEY = stringPreferencesKey("saved_username")

    suspend fun saveUsername(context: Context, username: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username
        }
    }

    // Returns a Flow that gives us the username (or null if not found)
    fun getUsername(context: Context) = context.dataStore.data.map { prefs ->
        prefs[USERNAME_KEY]
    }

    // Optional: clear on logout
    suspend fun clearUser(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(USERNAME_KEY)
        }
    }
}