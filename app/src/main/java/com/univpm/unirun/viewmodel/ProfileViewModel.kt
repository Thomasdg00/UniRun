package com.univpm.unirun.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class HistoryItem {
    data class MonthHeader(
        val label: String,
        val totalKm: Float,
        val isCurrentMonth: Boolean
    ) : HistoryItem()

    data class ActivityRow(
        val id: Long,
        val dayOfMonth: Int,
        val monthAbbr: String,
        val sportType: String,
        val timeOfDay: String,
        val titleName: String,
        val distanceKm: Float,
        val durationFormatted: String,
        val isCurrentMonth: Boolean
    ) : HistoryItem()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
    private val prefsRepo = UserPreferencesRepository(application)
    private val activitiesFlow = db.activityDao().observeAllByUser(uid)
    private val prefsFlow = prefsRepo.userPreferencesFlow
    private val sharing = SharingStarted.WhileSubscribed(5_000)

    val userName: StateFlow<String> = prefsFlow
        .map { prefs -> prefs.name.ifBlank { "ATHLETE" } }
        .stateIn(viewModelScope, sharing, "ATHLETE")

    val totalDistanceKm: StateFlow<Float> = activitiesFlow
        .map { activities -> (activities.sumOf { it.distanceMeters.toDouble() } / 1000.0).toFloat() }
        .stateIn(viewModelScope, sharing, 0f)

    val totalActivities: StateFlow<Int> = activitiesFlow
        .map { it.size }
        .stateIn(viewModelScope, sharing, 0)

    val avgPaceFormatted: StateFlow<String> = activitiesFlow
        .map(::computeAvgPace)
        .stateIn(viewModelScope, sharing, "--:--")

    val totalCalories: StateFlow<Int> = activitiesFlow
        .map { activities -> activities.sumOf { it.calories.toDouble() }.toInt() }
        .stateIn(viewModelScope, sharing, 0)

    val currentMonthLabel: StateFlow<String> = activitiesFlow
        .map { formatMonthLabel(System.currentTimeMillis()) }
        .stateIn(viewModelScope, sharing, formatMonthLabel(System.currentTimeMillis()))

    val monthlyKm: StateFlow<Float> = activitiesFlow
        .map { activities ->
            activities
                .filter { isSameCalendarMonth(it.startTimestamp, System.currentTimeMillis()) }
                .sumOf { it.distanceMeters.toDouble() }
                .div(1000.0)
                .toFloat()
        }
        .stateIn(viewModelScope, sharing, 0f)

    val monthlyTimeFormatted: StateFlow<String> = activitiesFlow
        .map { activities ->
            val totalSeconds = activities
                .filter { isSameCalendarMonth(it.startTimestamp, System.currentTimeMillis()) }
                .sumOf { it.durationSeconds }
            formatMonthTime(totalSeconds)
        }
        .stateIn(viewModelScope, sharing, "0:00 h")

    val groupedHistory: StateFlow<List<HistoryItem>> = activitiesFlow
        .map(::buildHistory)
        .stateIn(viewModelScope, sharing, emptyList())

    val joinedLabel: StateFlow<String> = combine(userName, prefsFlow) { _, _ ->
        val joinedAt = FirebaseAuth.getInstance().currentUser?.metadata?.creationTimestamp
            ?: System.currentTimeMillis()
        "JOINED ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(joinedAt)).uppercase(Locale.getDefault())}"
    }.stateIn(viewModelScope, sharing, "JOINED THIS MONTH")

    val locationLabel: StateFlow<String> = prefsFlow
        .map { "UNIRUN ATHLETE" }
        .stateIn(viewModelScope, sharing, "UNIRUN ATHLETE")

    private fun buildHistory(activities: List<ActivityEntity>): List<HistoryItem> {
        if (activities.isEmpty()) return emptyList()

        val sorted = activities.sortedByDescending { it.startTimestamp }
        val grouped = sorted.groupBy { bucketKey(it.startTimestamp) }
        val mostRecentBucket = bucketKey(sorted.first().startTimestamp)
        val monthAbbrFormatter = SimpleDateFormat("MMM", Locale.getDefault())

        return grouped.entries.flatMap { (bucket, bucketActivities) ->
            val first = bucketActivities.first()
            val isCurrentMonth = bucket == mostRecentBucket
            val header = HistoryItem.MonthHeader(
                label = formatMonthLabel(first.startTimestamp),
                totalKm = (bucketActivities.sumOf { it.distanceMeters.toDouble() } / 1000.0).toFloat(),
                isCurrentMonth = isCurrentMonth
            )

            val rows = bucketActivities.map { activity ->
                val title = timeOfDay(activity.startTimestamp, activity.sportType)
                HistoryItem.ActivityRow(
                    id = activity.id,
                    dayOfMonth = Calendar.getInstance().apply { timeInMillis = activity.startTimestamp }.get(Calendar.DAY_OF_MONTH),
                    monthAbbr = monthAbbrFormatter.format(Date(activity.startTimestamp)).uppercase(Locale.getDefault()),
                    sportType = activity.sportType,
                    timeOfDay = title,
                    titleName = title,
                    distanceKm = activity.distanceMeters / 1000f,
                    durationFormatted = formatDuration(activity.durationSeconds),
                    isCurrentMonth = isCurrentMonth
                )
            }

            listOf(header) + rows
        }
    }

    private fun bucketKey(epochMs: Long): Int {
        val calendar = Calendar.getInstance().apply { timeInMillis = epochMs }
        return calendar.get(Calendar.YEAR) * 100 + calendar.get(Calendar.MONTH)
    }

    private fun isSameCalendarMonth(firstMs: Long, secondMs: Long): Boolean {
        val first = Calendar.getInstance().apply { timeInMillis = firstMs }
        val second = Calendar.getInstance().apply { timeInMillis = secondMs }
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.MONTH) == second.get(Calendar.MONTH)
    }

    private fun formatMonthLabel(epochMs: Long): String {
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(epochMs)).uppercase(Locale.getDefault())
    }

    private fun computeAvgPace(activities: List<ActivityEntity>): String {
        val totalKm = activities.sumOf { it.distanceMeters.toDouble() } / 1000.0
        val totalSec = activities.sumOf { it.durationSeconds }
        if (totalKm < 0.1) return "--:--"
        val paceSec = (totalSec / totalKm).toInt()
        return "%d:%02d".format(paceSec / 60, paceSec % 60)
    }

    private fun formatDuration(seconds: Long): String =
        if (seconds >= 3600) {
            "%d:%02dh".format(seconds / 3600, (seconds % 3600) / 60)
        } else {
            "%d:%02d".format(seconds / 60, seconds % 60)
        }

    private fun formatMonthTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return "%d:%02d h".format(hours, minutes)
    }

    private fun timeOfDay(epochMs: Long, sportType: String): String {
        val hour = Calendar.getInstance().apply { timeInMillis = epochMs }.get(Calendar.HOUR_OF_DAY)
        val prefix = when {
            hour < 12 -> "Morning"
            hour < 17 -> "Afternoon"
            else -> "Evening"
        }
        val sport = when (sportType) {
            "BIKE" -> "Ride"
            "WALK" -> "Walk"
            else -> "Run"
        }
        return "$prefix $sport"
    }
}
