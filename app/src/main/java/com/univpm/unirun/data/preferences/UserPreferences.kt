package com.univpm.unirun.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferencesKeys {
    val NAME       = stringPreferencesKey("user_name")
    val WEIGHT_KG  = floatPreferencesKey("weight_kg")
    val HEIGHT_CM  = intPreferencesKey("height_cm")
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
}
