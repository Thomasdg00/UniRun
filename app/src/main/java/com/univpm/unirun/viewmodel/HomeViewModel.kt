package com.univpm.unirun.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
    val activities: Flow<List<ActivityEntity>> = db.activityDao().observeAllByUser(uid)

    // Computed StateFlows derived from activities
    val weeklyKm: StateFlow<Double> = activities.map { activities ->
        val nowMs = System.currentTimeMillis()
        val weekMs = TimeUnit.DAYS.toMillis(7)
        val thisWeek = activities.filter { nowMs - it.startTimestamp <= weekMs }
        thisWeek.sumOf { it.distanceMeters.toDouble() } / 1000.0
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000L), 0.0)

    val activeTimeFormatted: StateFlow<String> = activities.map { activities ->
        val nowMs = System.currentTimeMillis()
        val weekMs = TimeUnit.DAYS.toMillis(7)
        val thisWeek = activities.filter { nowMs - it.startTimestamp <= weekMs }
        val totalSeconds = thisWeek.sumOf { it.durationSeconds }
        formatHoursMinutes(totalSeconds)
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000L), "00:00")

    val activeTimeDelta: StateFlow<String> = activities.map { activities ->
        val nowMs = System.currentTimeMillis()
        val weekMs = TimeUnit.DAYS.toMillis(7)
        val thisWeek = activities.filter { nowMs - it.startTimestamp <= weekMs }
        val lastWeek = activities.filter {
            val elapsed = nowMs - it.startTimestamp
            elapsed > weekMs && elapsed <= weekMs * 2
        }
        val thisWeekSeconds = thisWeek.sumOf { it.durationSeconds }
        val lastWeekSeconds = lastWeek.sumOf { it.durationSeconds }
        when {
            thisWeekSeconds == 0L && lastWeekSeconds == 0L -> "No activity yet"
            lastWeekSeconds <= 0L -> "First week!"
            else -> {
                val delta = (((thisWeekSeconds - lastWeekSeconds).toDouble() / lastWeekSeconds) * 100).roundToInt()
                String.format(Locale.getDefault(), "%+d%% vs LW", delta)
            }
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000L), "First week!")

    val intensityKcal: StateFlow<Int> = activities.map { activities ->
        val nowMs = System.currentTimeMillis()
        val weekMs = TimeUnit.DAYS.toMillis(7)
        val thisWeek = activities.filter { nowMs - it.startTimestamp <= weekMs }
        thisWeek.sumOf { it.calories.toDouble() }.roundToInt()
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000L), 0)

    val streak: StateFlow<Int> = activities.map { activities ->
        computeStreak(activities)
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000L), 0)

    val recoveryStatus: StateFlow<String> = activities.map { activities ->
        val lastActivity = activities.maxByOrNull { it.startTimestamp }
        val nowMs = System.currentTimeMillis()
        computeRecoveryStatus(lastActivity, nowMs)
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000L), "READY TO PUSH")

    val recentActivities: StateFlow<List<ActivityEntity>> = activities.map { activities ->
        activities.sortedByDescending { it.startTimestamp }.take(10)
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000L), emptyList())

    private fun computeStreak(activities: List<ActivityEntity>): Int {
        if (activities.isEmpty()) return 0
        val activeEpochDays = activities.map { epochDayFromTimestamp(it.startTimestamp) }.toSet()
        var streak = 0
        var currentDay = epochDayFromTimestamp(System.currentTimeMillis())
        while (activeEpochDays.contains(currentDay)) {
            streak++
            currentDay--
        }
        return streak
    }

    private fun computeRecoveryStatus(lastActivity: ActivityEntity?, nowMs: Long): String {
        if (lastActivity == null) return "READY TO PUSH"
        val hoursSinceLast = TimeUnit.MILLISECONDS.toHours(nowMs - lastActivity.startTimestamp)
        return when {
            hoursSinceLast >= 36 -> "READY TO PUSH"
            hoursSinceLast >= 18 -> "BALANCED LOAD"
            else -> "RECOVERING NOW"
        }
    }

    private fun epochDayFromTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis)
    }

    private fun formatHoursMinutes(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }
}