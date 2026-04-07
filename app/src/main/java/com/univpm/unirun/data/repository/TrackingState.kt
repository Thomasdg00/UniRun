package com.univpm.unirun.data.repository

data class LatLng(val lat: Double, val lon: Double)

enum class TrackingStatus { IDLE, RUNNING, PAUSED }

data class TrackingState(
    val status: TrackingStatus = TrackingStatus.IDLE,
    val elapsedSeconds: Long = 0L,
    val distanceMeters: Float = 0f,
    val paceSecPerKm: Int = 0,
    val currentSpeedKmh: Float = 0f,
    val pathPoints: List<LatLng> = emptyList(),
    val currentLatLng: LatLng? = null
) {
    val formattedPace: String
        get() {
            if (paceSecPerKm == 0) return "--:--"
            val m = paceSecPerKm / 60
            val s = paceSecPerKm % 60
            return String.format("%d:%02d", m, s)
        }

    val formattedDistance: String
        get() {
            return if (distanceMeters < 1000f) {
                String.format("%.0f m", distanceMeters)
            } else {
                String.format("%.2f km", distanceMeters / 1000f)
            }
        }

    val formattedDuration: String
        get() {
            val h = elapsedSeconds / 3600
            val m = (elapsedSeconds % 3600) / 60
            val s = elapsedSeconds % 60
            return if (h > 0) {
                String.format("%d:%02d:%02d", h, m, s)
            } else {
                String.format("%02d:%02d", m, s)
            }
        }
}
