package com.univpm.unirun.ui.tracking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.univpm.unirun.R
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import com.univpm.unirun.data.repository.TrackingState
import com.univpm.unirun.data.repository.TrackingStatus
import com.univpm.unirun.utils.PermissionManager
import com.univpm.unirun.viewmodel.TrackingViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val trackingViewModel: TrackingViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var tvDuration: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvPace: TextView
    private lateinit var tvHeartRate: TextView
    private lateinit var tvElevation: TextView
    private lateinit var tvCalories: TextView
    private lateinit var btnPauseResume: MaterialButton
    private lateinit var btnStop: MaterialButton

    private var sportType: String = "RUN"
    private var weightKg: Float = 70f
    private var latestState: TrackingState = TrackingState()
    private var polylineAnnotationManager: PolylineAnnotationManager? = null
    private var currentPolylineAnnotation: PolylineAnnotation? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            startTrackingIfPossible()
        } else {
            Snackbar.make(requireView(), "Permessi di posizione necessari", Snackbar.LENGTH_LONG)
                .setAction("Impostazioni") {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireContext().packageName, null)
                    }
                    startActivity(intent)
                }
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sportType = arguments?.getString("sportType") ?: "RUN"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupChrome(view)
        setupMap()
        loadWeight()
        observeTracking()
        startTrackingIfPossible()
    }

    private fun bindViews(view: View) {
        mapView = view.findViewById(R.id.mapView)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvDistance = view.findViewById(R.id.tvDistance)
        tvPace = view.findViewById(R.id.tvPace)
        tvHeartRate = view.findViewById(R.id.tvHeartRate)
        tvElevation = view.findViewById(R.id.tvElevation)
        tvCalories = view.findViewById(R.id.tvCalories)
        btnPauseResume = view.findViewById(R.id.btnPauseResume)
        btnStop = view.findViewById(R.id.btnStop)
    }

    private fun setupChrome(view: View) {
        view.findViewById<View>(R.id.btnMenuTracking).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.btnSettingsTracking).setOnClickListener { }
        view.findViewById<View>(R.id.btnMyLocation).setOnClickListener {
            latestState.currentLatLng?.let { latLng ->
                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(latLng.lon, latLng.lat))
                        .zoom(15.0)
                        .build()
                )
            }
        }
        view.findViewById<View>(R.id.btnMapLayers).setOnClickListener { }
        view.findViewById<View>(R.id.navHomeTracking).setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        view.findViewById<View>(R.id.navProfileTracking).setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        btnPauseResume.setOnClickListener {
            when (latestState.status) {
                TrackingStatus.RUNNING -> trackingViewModel.pauseTracking()
                TrackingStatus.PAUSED -> trackingViewModel.resumeTracking()
                TrackingStatus.IDLE -> Unit
            }
        }
        btnStop.setOnClickListener { showStopConfirmationDialog() }

        tvHeartRate.text = "--"
        tvElevation.text = "--"
    }

    private fun setupMap() {
        mapView.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            polylineAnnotationManager = mapView.annotations.createPolylineAnnotationManager()
            updatePolyline(latestState)
        }
    }

    private fun loadWeight() {
        viewLifecycleOwner.lifecycleScope.launch {
            weightKg = UserPreferencesRepository(requireContext()).userPreferencesFlow.first().weightKg
            updateCalories(latestState)
        }
    }

    private fun observeTracking() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                trackingViewModel.trackingState.collect { state ->
                    latestState = state
                    updateDashboard(state)
                    updatePauseResumeButton(state)
                    updatePolyline(state)
                }
            }
        }
    }

    private fun startTrackingIfPossible() {
        if (trackingViewModel.trackingState.value.status != TrackingStatus.IDLE) return

        if (PermissionManager.hasLocationPermissions(requireContext()) &&
            PermissionManager.hasNotificationPermission(requireContext())
        ) {
            trackingViewModel.startTracking(sportType)
        } else {
            val missingPermissions = PermissionManager.getMissingPermissions(requireActivity())
            if (missingPermissions.isNotEmpty()) {
                requestPermissionLauncher.launch(missingPermissions)
            }
        }
    }

    private fun updateDashboard(state: TrackingState) {
        tvDuration.text = state.formattedDuration
        tvDistance.text = String.format(Locale.getDefault(), "%.2f", state.distanceMeters / 1000f)
        tvPace.text = state.formattedPace
        updateCalories(state)
    }

    private fun updateCalories(state: TrackingState) {
        val met = when (state.sportType) {
            "RUN" -> 9.8f
            "WALK" -> 3.5f
            "BIKE" -> 7.5f
            else -> 7.5f
        }
        val calories = met * weightKg * (state.elapsedSeconds / 3600f)
        tvCalories.text = String.format(Locale.getDefault(), "%.0f", calories)
    }

    private fun updatePauseResumeButton(state: TrackingState) {
        when (state.status) {
            TrackingStatus.PAUSED -> {
                btnPauseResume.isEnabled = true
                btnPauseResume.text = "RESUME SESSION"
                btnPauseResume.setIconResource(android.R.drawable.ic_media_play)
            }
            TrackingStatus.RUNNING -> {
                btnPauseResume.isEnabled = true
                btnPauseResume.text = "PAUSE SESSION"
                btnPauseResume.setIconResource(android.R.drawable.ic_media_pause)
            }
            TrackingStatus.IDLE -> {
                btnPauseResume.isEnabled = false
                btnPauseResume.text = "PAUSE SESSION"
                btnPauseResume.setIconResource(android.R.drawable.ic_media_pause)
            }
        }
    }

    private fun updatePolyline(state: TrackingState) {
        if (state.pathPoints.isEmpty()) return

        val manager = polylineAnnotationManager ?: return
        val points = state.pathPoints.map { Point.fromLngLat(it.lon, it.lat) }

        if (currentPolylineAnnotation == null) {
            val options = PolylineAnnotationOptions()
                .withPoints(points)
                .withLineColor("#EE4B2B")
                .withLineWidth(5.0)
            currentPolylineAnnotation = manager.create(options)
        } else {
            currentPolylineAnnotation?.points = points
            currentPolylineAnnotation?.let(manager::update)
        }

        state.currentLatLng?.let { latLng ->
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(latLng.lon, latLng.lat))
                    .zoom(15.0)
                    .build()
            )
        }
    }

    private fun showStopConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Terminare allenamento?")
            .setMessage("Vuoi terminare l'allenamento?")
            .setPositiveButton("Sì") { _, _ ->
                trackingViewModel.stopTracking()
                findNavController().navigate(R.id.action_tracking_to_post_run)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}
