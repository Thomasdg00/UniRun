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
            _dailyGoalMl.value = (prefs.weightKg * 33).toInt().coerceIn(1500, 4000)
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.waterLogDao().deleteOldLogs(uid, today)
        }
    }

    fun addWater(ml: Int = 250) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = db.waterLogDao().observeForDay(uid, today).first()?.mlConsumed ?: 0
            db.waterLogDao().upsert(
                WaterLogEntity(
                    userId = uid,
                    dateEpochDay = today,
                    mlConsumed = current + ml
                )
            )
        }
    }

    fun removeWater(ml: Int = 250) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = db.waterLogDao().observeForDay(uid, today).first()?.mlConsumed ?: 0
            val newValue = (current - ml).coerceAtLeast(0)
            db.waterLogDao().upsert(
                WaterLogEntity(
                    userId = uid,
                    dateEpochDay = today,
                    mlConsumed = newValue
                )
            )
        }
    }
}
