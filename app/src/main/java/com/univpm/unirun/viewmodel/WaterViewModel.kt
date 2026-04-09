package com.univpm.unirun.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.data.db.WaterLogEntity
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

class WaterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
    private val today = LocalDate.now().toEpochDay()

    private val _dailyGoalMl = MutableStateFlow(2000)
    val dailyGoalMl: StateFlow<Int> = _dailyGoalMl.asStateFlow()

    val todayLog: Flow<WaterLogEntity?> = db.waterLogDao().observeForDay(uid, today)

    val consumedMl: Flow<Int> = todayLog.map { it?.mlConsumed ?: 0 }

    init {
        viewModelScope.launch {
            val prefs = UserPreferencesRepository(application).userPreferencesFlow.first()
            // ✅ COSTANTE DENOMINATA: Formula WHO consigliata
            // ~33 ml per kg di peso corporeo (varia da 30 a 35 ml/kg)
            _dailyGoalMl.value = (prefs.weightKg * WATER_ML_PER_KG_BODY_WEIGHT).toInt()
                .coerceIn(WATER_MIN_DAILY_ML, WATER_MAX_DAILY_ML)
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.waterLogDao().deleteOldLogs(uid, today)
        }
    }

    fun addWater(ml: Int = 250) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = db.waterLogDao().observeForDay(uid, today).first()
            db.waterLogDao().upsert(
                WaterLogEntity(
                    id = current?.id ?: 0,
                    userId = uid,
                    dateEpochDay = today,
                    mlConsumed = (current?.mlConsumed ?: 0) + ml
                )
            )
        }
    }

    fun removeWater(ml: Int = 250) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = db.waterLogDao().observeForDay(uid, today).first()
            val newValue = ((current?.mlConsumed ?: 0) - ml).coerceAtLeast(0)
            db.waterLogDao().upsert(
                WaterLogEntity(
                    id = current?.id ?: 0,
                    userId = uid,
                    dateEpochDay = today,
                    mlConsumed = newValue
                )
            )
        }
    }

    companion object {
        // ✅ Costanti per il calcolo dell'obiettivo di acqua giornaliero
        // Formula WHO: 30-35 ml per kg di peso corporeo
        // Usiamo 33 come valore medio
        private const val WATER_ML_PER_KG_BODY_WEIGHT = 33f

        // Limiti ragionevoli per il consumo di acqua
        private const val WATER_MIN_DAILY_ML = 1500  // ~6 bicchieri
        private const val WATER_MAX_DAILY_ML = 4000  // ~16 bicchieri
    }
}