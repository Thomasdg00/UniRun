package com.univpm.unirun.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.univpm.unirun.data.repository.LatLng
import com.univpm.unirun.data.repository.TrackingRepository
import com.univpm.unirun.data.repository.TrackingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrackingService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        private const val CHANNEL_ID = "unirun_tracking"
        private const val NOTIFICATION_ID = 1
        private const val GPS_MIN_ACCURACY_M = 25f
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null
    private var isTimerRunning = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            for (location in result.locations) {
                if (location.accuracy <= GPS_MIN_ACCURACY_M) {
                    TrackingRepository.updateLocation(
                        LatLng(location.latitude, location.longitude),
                        location.altitude
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, buildNotification("Registrazione in corso..."))
                handleStart()
            }
            ACTION_PAUSE -> {
                handlePause()
            }
            ACTION_RESUME -> {
                startForeground(NOTIFICATION_ID, buildNotification("Registrazione in corso..."))
                handleResume()
            }
            ACTION_STOP -> {
                handleStop()
            }
            else -> {
                if (TrackingRepository.state.value.status == TrackingStatus.RUNNING) {
                    startForeground(NOTIFICATION_ID, buildNotification("Registrazione in corso..."))
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun handleStart() {
        val sportType = TrackingRepository.state.value.sportType.ifEmpty { "RUN" }
        if (TrackingRepository.state.value.status != TrackingStatus.RUNNING) {
            TrackingRepository.startTracking(sportType)
        }
        startLocationUpdates()
        startTimer()
    }

    private fun handlePause() {
        TrackingRepository.pauseTracking()
        stopLocationUpdates()
        stopTimer()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification("In pausa — tocca per riprendere"))
    }

    private fun handleResume() {
        TrackingRepository.resumeTracking()
        startLocationUpdates()
        startTimer()
    }

    private fun handleStop() {
        stopLocationUpdates()
        stopTimer()
        TrackingRepository.stopTracking()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).apply {
            setMinUpdateDistanceMeters(3f)
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startTimer() {
        if (isTimerRunning) return
        isTimerRunning = true
        timerJob = serviceScope.launch {
            while (isTimerRunning) {
                delay(1000L)
                if (TrackingRepository.state.value.status == TrackingStatus.RUNNING) {
                    val currentElapsed = TrackingRepository.state.value.elapsedSeconds
                    TrackingRepository.updateElapsed(currentElapsed + 1)
                }
            }
        }
    }

    private fun stopTimer() {
        isTimerRunning = false
        timerJob?.cancel()
        timerJob = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "UniRun Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifica attiva durante il tracciamento GPS"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("UniRun")
        .setContentText(text)
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setOngoing(true)
        .setSilent(true)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        stopLocationUpdates()
        serviceScope.cancel()
    }
}
