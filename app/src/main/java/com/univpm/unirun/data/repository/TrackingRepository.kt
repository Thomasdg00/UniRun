package com.univpm.unirun.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object TrackingRepository {
    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    fun startTracking(sportType: String = "RUN") {
        _state.value = TrackingState(
            status = TrackingStatus.RUNNING,
            sportType = sportType
        )
    }

    fun pauseTracking() {
        _state.update { it.copy(status = TrackingStatus.PAUSED) }
    }

    fun resumeTracking() {
        _state.update { it.copy(status = TrackingStatus.RUNNING) }
    }

    fun stopTracking() {
        _state.update { it.copy(status = TrackingStatus.IDLE) }
    }

    fun resetState() {
        _state.value = TrackingState()
    }

    fun updateLocation(newLatLng: LatLng, altitudeM: Double) {
        val currentState = _state.value
        if (currentState.status != TrackingStatus.RUNNING) return

        val newPathPoints = currentState.pathPoints + newLatLng
        
        var addedDistance = 0f
        if (currentState.pathPoints.isNotEmpty()) {
            val lastPoint = currentState.pathPoints.last()
            addedDistance = haversineMeters(lastPoint, newLatLng)
        }
        
        val newDistanceMeters = currentState.distanceMeters + addedDistance
        val newPace = calculatePace(newDistanceMeters, currentState.elapsedSeconds)
        
        val speedKmh = if (currentState.elapsedSeconds > 0) {
            (newDistanceMeters / 1000f) / (currentState.elapsedSeconds / 3600f)
        } else {
            0f
        }

        _state.update {
            it.copy(
                pathPoints = newPathPoints,
                currentLatLng = newLatLng,
                distanceMeters = newDistanceMeters,
                paceSecPerKm = newPace,
                currentSpeedKmh = speedKmh
            )
        }
    }

    fun updateElapsed(seconds: Long) {
        val currentState = _state.value
        val newPace = calculatePace(currentState.distanceMeters, seconds)
        val speedKmh = if (seconds > 0) {
            (currentState.distanceMeters / 1000f) / (seconds / 3600f)
        } else {
            0f
        }
        
        _state.update {
            it.copy(
                elapsedSeconds = seconds,
                paceSecPerKm = newPace,
                currentSpeedKmh = speedKmh
            )
        }
    }

    private fun haversineMeters(from: LatLng, to: LatLng): Float {
        val R = 6371000.0
        val dLat = Math.toRadians(to.lat - from.lat)
        val dLon = Math.toRadians(to.lon - from.lon)
        val fromLatRad = Math.toRadians(from.lat)
        val toLatRad = Math.toRadians(to.lat)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(fromLatRad) * cos(toLatRad) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (R * c).toFloat()
    }

    private fun calculatePace(distanceMeters: Float, elapsedSeconds: Long): Int {
        // ✅ PROTEZIONE MIGLIORATA: Verificare sia distanza che tempo
        if (distanceMeters < MIN_DISTANCE_FOR_PACE_CALCULATION || elapsedSeconds == 0L) {
            return 0
        }
        // sec / km
        return (elapsedSeconds / (distanceMeters / 1000f)).toInt()
    }

    companion object {
        // ✅ Costante denominata per il calcolo della pace
        // Minima distanza richiesta per calcolare un pace significativo (20 metri)
        private const val MIN_DISTANCE_FOR_PACE_CALCULATION = 20f
    }
}