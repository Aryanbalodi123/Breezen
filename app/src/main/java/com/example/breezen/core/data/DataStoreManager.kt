package com.example.breezen.core.data

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.time.LocalDate

// ------- DATASTORE INSTANCE -------
// ------- Holds persistent app settings -------
val Context.dataStore by preferencesDataStore(name = "app_settings")

// ------- MOOD PREFERENCE -------
// ------- Stores daily mood & resets each day -------
object MoodPreference {

    private val MOOD_BOOLEAN = booleanPreferencesKey("mood_boolean")
    private val MOOD_SET_DATE = stringPreferencesKey("mood_set_date")

    // Save mood state + today's date
    suspend fun saveMoodState(context: Context, value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MOOD_BOOLEAN] = value

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                prefs[MOOD_SET_DATE] = LocalDate.now().toString()
            }
        }
    }

    // Observe mood state (auto resets if date changed)
    fun observeMoodState(context: Context) =
        context.dataStore.data.map { prefs ->

            val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate.now().toString()
            } else ""

            val savedDate = prefs[MOOD_SET_DATE]

            if (savedDate == today) {
                prefs[MOOD_BOOLEAN] ?: false
            } else {
                false
            }
        }
}

// ------- USER PREFERENCES -------
// ------- Stores & retrieves username -------
object UserPreferences {

    private val USERNAME_KEY = stringPreferencesKey("saved_username")

    suspend fun saveUsername(context: Context, username: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username
        }
    }

    fun getUsername(context: Context) = context.dataStore.data.map { prefs ->
        prefs[USERNAME_KEY]
    }

    suspend fun clearUser(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(USERNAME_KEY)
        }
    }
    suspend fun clearAll(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

}
