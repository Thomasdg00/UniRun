package com.univpm.unirun.ui.tracking

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.univpm.unirun.R
import com.univpm.unirun.data.repository.LatLng
import com.univpm.unirun.data.repository.TrackingState
import com.univpm.unirun.data.repository.TrackingStatus
import com.univpm.unirun.utils.PermissionManager
import com.univpm.unirun.viewmodel.TrackingViewModel
import kotlinx.coroutines.launch

class TrackingFragment : Fragment() {

    private val trackingViewModel: TrackingViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var tvDuration: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvPace: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnResume: Button
    private lateinit var btnStop: Button

    private var polylineAnnotationManager: PolylineAnnotationManager? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var granted = true
        permissions.entries.forEach {
            if (!it.value) granted = false
        }
        if (!granted) {
            Snackbar.make(requireView(), "Permessi di posizione necessari", Snackbar.LENGTH_LONG)
                .setAction("Impostazioni") {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }.show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tracking, container, false)

        mapView = view.findViewById(R.id.mapView)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvDistance = view.findViewById(R.id.tvDistance)
        tvPace = view.findViewById(R.id.tvPace)
        tvSpeed = view.findViewById(R.id.tvSpeed)
        btnStart = view.findViewById(R.id.btnStart)
        btnPause = view.findViewById(R.id.btnPause)
        btnResume = view.findViewById(R.id.btnResume)
        btnStop = view.findViewById(R.id.btnStop)

        mapView.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            val annotationApi = mapView.annotations
            polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()
        }

        val sportType = arguments?.getString("sportType") ?: "RUN"
        btnStart.setOnClickListener { trackingViewModel.startTracking(sportType) }
        btnPause.setOnClickListener { trackingViewModel.pauseTracking() }
        btnResume.setOnClickListener { trackingViewModel.resumeTracking() }
        btnStop.setOnClickListener { showStopConfirmationDialog() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                trackingViewModel.trackingState.collect { state ->
                    updateDashboard(state)
                    updateButtons(state)
                    updatePolyline(state)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()

        if (!PermissionManager.hasLocationPermissions(requireContext())) {
            val missingPermissions = PermissionManager.getMissingPermissions(requireActivity())
            if (missingPermissions.isNotEmpty()) {
                requestPermissionLauncher.launch(missingPermissions)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    private fun updateDashboard(state: TrackingState) {
        tvDuration.text = state.formattedDuration
        tvDistance.text = state.formattedDistance
        tvPace.text = state.formattedPace
        tvSpeed.text = "%.1f km/h".format(state.currentSpeedKmh)
    }

    private fun updateButtons(state: TrackingState) {
        when (state.status) {
            TrackingStatus.IDLE -> {
                btnStart.visibility = View.VISIBLE
                btnPause.visibility = View.GONE
                btnResume.visibility = View.GONE
                btnStop.visibility = View.GONE
            }
            TrackingStatus.RUNNING -> {
                btnStart.visibility = View.GONE
                btnPause.visibility = View.VISIBLE
                btnResume.visibility = View.GONE
                btnStop.visibility = View.VISIBLE
            }
            TrackingStatus.PAUSED -> {
                btnStart.visibility = View.GONE
                btnPause.visibility = View.GONE
                btnResume.visibility = View.VISIBLE
                btnStop.visibility = View.VISIBLE
            }
        }
    }

    private fun updatePolyline(state: TrackingState) {
        if (state.pathPoints.isNotEmpty()) {
            val points = state.pathPoints.map { Point.fromLngLat(it.lon, it.lat) }
            polylineAnnotationManager?.deleteAll()
            val polylineAnnotationOptions = PolylineAnnotationOptions()
                .withPoints(points)
                .withLineColor("#EE4B2B")
                .withLineWidth(5.0)

            polylineAnnotationManager?.create(polylineAnnotationOptions)

            state.currentLatLng?.let { latLng ->
                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(latLng.lon, latLng.lat))
                        .zoom(15.0)
                        .build()
                )
            }
        }
    }

    private fun showStopConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Terminare allenamento?")
            .setMessage("Vuoi terminare l'allenamento?")
            .setPositiveButton("Sì") { _, _ ->
                trackingViewModel.stopTracking()
                // NON fare resetState qui: PostRunFragment legge ancora i dati
                findNavController().navigate(R.id.action_tracking_to_post_run)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}
