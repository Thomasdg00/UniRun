package com.univpm.unirun.ui.tracking

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.material.snackbar.Snackbar
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import com.univpm.unirun.data.repository.TrackingRepository
import com.univpm.unirun.data.repository.TrackingStatus
import com.univpm.unirun.service.TrackingService
import com.univpm.unirun.utils.PermissionManager
import com.univpm.unirun.viewmodel.TrackingViewModel
import kotlinx.coroutines.flow.first

@Composable
fun TrackingRoute(
    trackingViewModel: TrackingViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val trackingState by trackingViewModel.trackingState.collectAsStateWithLifecycle(initialValue = TrackingRepository.state.value)
    
    var sportType by remember { mutableStateOf("RUN") }
    var weightKg by remember { mutableStateOf(70f) }
    var trackingStarted by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }

    // Load weight on first composition
    LaunchedEffect(Unit) {
        val prefs = UserPreferencesRepository(context).userPreferencesFlow.first()
        weightKg = prefs.weightKg
    }

    // Initialize sport type from repository
    LaunchedEffect(Unit) {
        val currentStatus = TrackingRepository.state.value.status
        if (currentStatus == TrackingStatus.IDLE) {
            sportType = TrackingRepository.state.value.sportType.ifEmpty { "RUN" }
        } else {
            sportType = TrackingRepository.state.value.sportType
            trackingStarted = true
        }

        // Start tracking if not already started
        if (!trackingStarted) {
            if (PermissionManager.hasLocationPermissions(context) &&
                PermissionManager.hasNotificationPermission(context)
            ) {
                trackingStarted = true
                trackingViewModel.startTracking(sportType)
            }
        }
    }

    TrackingScreen(
        trackingState = trackingState,
        sportType = sportType,
        onSportTypeChange = { newSport ->
            if (trackingState.status == TrackingStatus.IDLE && !trackingStarted) {
                sportType = newSport
            }
        },
        onPauseResumeClick = {
            when (trackingState.status) {
                TrackingStatus.RUNNING -> trackingViewModel.pauseTracking()
                TrackingStatus.PAUSED -> trackingViewModel.resumeTracking()
                TrackingStatus.IDLE -> Unit
            }
        },
        onStopClick = {
            trackingViewModel.stopTracking()
            navController.navigate("post_run") {
                popUpTo("tracking") { inclusive = true }
            }
        },
        onShowStopDialog = { show -> showStopDialog = show },
        showStopDialog = showStopDialog,
        onBackClick = {
            navController.navigateUp()
        },
        onHomeClick = {
            navController.navigate("home") {
                popUpTo("tracking") { inclusive = true }
            }
        },
        onProfileClick = {
            navController.navigate("profile") {
                popUpTo("tracking") { inclusive = true }
            }
        },
        onMyLocationClick = {
            trackingState.currentLatLng?.let { latLng ->
                // Map would center on user location
            }
        },
        modifier = modifier
    )
}
