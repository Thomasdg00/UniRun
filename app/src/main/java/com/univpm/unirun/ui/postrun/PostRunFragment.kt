package com.univpm.unirun.ui.postrun

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.univpm.unirun.R
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.data.repository.ActivityRepository
import com.univpm.unirun.data.repository.TrackingRepository
import com.univpm.unirun.data.repository.TrackingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostRunFragment : Fragment(R.layout.fragment_post_run) {

    private lateinit var mapViewStatic: MapView
    private lateinit var tvSportType: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var tvAvgPace: TextView
    private lateinit var tvTotalCalories: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapViewStatic = view.findViewById(R.id.mapViewStatic)
        tvSportType = view.findViewById(R.id.tvSportType)
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance)
        tvTotalDuration = view.findViewById(R.id.tvTotalDuration)
        tvAvgPace = view.findViewById(R.id.tvAvgPace)
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories)

        val btnSaveActivity = view.findViewById<Button>(R.id.btnSaveActivity)
        val btnDiscardActivity = view.findViewById<Button>(R.id.btnDiscardActivity)

        val state = TrackingRepository.state.value

        tvSportType.text = when(state.sportType) {
            "RUN" -> "🏃 Corsa"
            "WALK" -> "🚶 Camminata"
            "BIKE" -> "🚴 Bici"
            else -> state.sportType
        }
        tvTotalDistance.text = state.formattedDistance
        tvTotalDuration.text = state.formattedDuration
        tvAvgPace.text = state.formattedPace
        
        val weightKg = 70f // placeholder, Parte 4 leggerà dal profilo
        val calories = calculateCalories(state, weightKg)
        tvTotalCalories.text = "%.0f kcal".format(calories)

        mapViewStatic.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            drawPolyline(state)
        }

        btnSaveActivity.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(requireContext())
                    val repo = ActivityRepository(db)
                    repo.saveActivity(
                        userId = "local_user",
                        sportType = state.sportType,
                        durationSeconds = state.elapsedSeconds,
                        distanceMeters = state.distanceMeters,
                        pathPoints = state.pathPoints,
                        weightKg = weightKg
                    )
                }
                TrackingRepository.resetState()
                findNavController().navigate(R.id.action_post_run_to_home)
            }
        }

        btnDiscardActivity.setOnClickListener {
            TrackingRepository.resetState()
            findNavController().navigate(R.id.action_post_run_to_home)
        }
    }

    private fun calculateCalories(state: TrackingState, weightKg: Float): Float {
        val met = when (state.sportType) {
            "RUN"  -> 9.8f
            "WALK" -> 3.5f
            "BIKE" -> 7.5f
            else   -> 7.5f
        }
        return met * weightKg * (state.elapsedSeconds / 3600f)
    }

    private fun drawPolyline(state: TrackingState) {
        if (state.pathPoints.isNotEmpty()) {
            val points = state.pathPoints.map { Point.fromLngLat(it.lon, it.lat) }
            val annotationManager = mapViewStatic.annotations.createPolylineAnnotationManager()
            val polylineAnnotationOptions = PolylineAnnotationOptions()
                .withPoints(points)
                .withLineColor("#EE4B2B")
                .withLineWidth(5.0)

            annotationManager.create(polylineAnnotationOptions)

            // Center camera on the middle point or last point
            val centerPoint = points[points.size / 2]
            mapViewStatic.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(centerPoint)
                    .zoom(14.0)
                    .build()
            )
        }
    }

    override fun onStart() {
        super.onStart()
        mapViewStatic.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapViewStatic.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapViewStatic.onDestroy()
    }
}
