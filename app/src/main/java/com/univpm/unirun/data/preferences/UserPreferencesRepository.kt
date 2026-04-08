package com.univpm.unirun.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPrefs(
    val name: String,
    val weightKg: Float,
    val heightCm: Int,
    val onboardingDone: Boolean
)

class UserPreferencesRepository(private val context: Context) {

    private val dataStore = context.dataStore

    val userPreferencesFlow: Flow<UserPrefs> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            UserPrefs(
                name = prefs[UserPreferencesKeys.NAME] ?: "",
                weightKg = prefs[UserPreferencesKeys.WEIGHT_KG] ?: 70f,
                heightCm = prefs[UserPreferencesKeys.HEIGHT_CM] ?: 170,
                onboardingDone = prefs[UserPreferencesKeys.ONBOARDING_DONE] ?: false
            )
        }

    suspend fun saveProfile(name: String, weightKg: Float, heightCm: Int) {
        dataStore.edit { prefs ->
            prefs[UserPreferencesKeys.NAME] = name
            prefs[UserPreferencesKeys.WEIGHT_KG] = weightKg
            prefs[UserPreferencesKeys.HEIGHT_CM] = heightCm
            prefs[UserPreferencesKeys.ONBOARDING_DONE] = true
        }
    }
}
