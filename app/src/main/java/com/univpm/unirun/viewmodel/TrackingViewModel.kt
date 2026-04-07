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
        sendServiceAction(TrackingService.ACTION_STOP)
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(getApplication(), TrackingService::class.java).apply {
            this.action = action
        }
        ContextCompat.startForegroundService(getApplication(), intent)
    }
}
