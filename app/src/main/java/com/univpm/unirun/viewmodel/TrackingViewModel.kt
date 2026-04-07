package com.univpm.unirun.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.univpm.unirun.data.repository.TrackingRepository
import com.univpm.unirun.data.repository.TrackingState
import com.univpm.unirun.data.repository.TrackingStatus
import com.univpm.unirun.service.TrackingService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.data.db.ActivityEntity
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackingViewModel(application: Application) : AndroidViewModel(application) {

    val trackingState: StateFlow<TrackingState> = TrackingRepository.state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TrackingState()
    )

    val isTracking: Boolean
        get() = trackingState.value.status == TrackingStatus.RUNNING

    val isPaused: Boolean
        get() = trackingState.value.status == TrackingStatus.PAUSED

    fun startTracking(sportType: String) {
        // Here you could save sportType into Repository or start action.
        sendServiceAction(TrackingService.ACTION_START)
    }

    fun pauseTracking() {
        sendServiceAction(TrackingService.ACTION_PAUSE)
    }

    fun resumeTracking() {
        sendServiceAction(TrackingService.ACTION_RESUME)
    }

    fun stopTracking() {
        viewModelScope.launch {
            val state = TrackingRepository.state.value
            val entity = ActivityEntity(
                userId = "local_user",
                sportType = "RUN",
                startTimestamp = System.currentTimeMillis() - (state.elapsedSeconds * 1000L),
                durationSeconds = state.elapsedSeconds,
                distanceMeters = state.distanceMeters,
                calories = 0f,
                polylineJson = "",
                gearId = null
            )
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance(getApplication()).activityDao().insert(entity)
            }
            sendServiceAction(TrackingService.ACTION_STOP)
            TrackingRepository.resetState()
        }
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(getApplication(), TrackingService::class.java).apply {
            this.action = action
        }
        ContextCompat.startForegroundService(getApplication(), intent)
    }
}
