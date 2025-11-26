package com.example.breezen.core.data


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.time.LocalDate

val Context.dataStore by preferencesDataStore("app_settings")


object AppPreferences {

    private val MY_BOOLEAN = booleanPreferencesKey("my_boolean")

    suspend fun setMyBoolean(context: Context, value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MY_BOOLEAN] = value
        }
    }

    fun getMyBoolean(context: Context) =
        context.dataStore.data.map { prefs ->
            prefs[MY_BOOLEAN] ?: false        // default = false
        }
}

object MoodPreference {
    private val MOOD_BOOLEAN = booleanPreferencesKey("mood_boolean")
    private val MOOD_SET_DATE = stringPreferencesKey("mood_set_date")

    suspend fun setMoodBoolean(context :Context , value: Boolean){
        context.dataStore.edit { prefs ->
            prefs[MOOD_BOOLEAN] = value
        prefs[MOOD_SET_DATE] = LocalDate.now().toString()
        }}

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
