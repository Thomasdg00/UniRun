package com.univpm.unirun.ui.postrun

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.univpm.unirun.R
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import com.univpm.unirun.data.repository.ActivityRepository
import com.univpm.unirun.data.repository.LatLng
import com.univpm.unirun.data.repository.TrackingRepository
import com.univpm.unirun.data.repository.TrackingState
import com.univpm.unirun.viewmodel.GearViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class PostRunFragment : Fragment(R.layout.fragment_post_run) {

    private val gearViewModel: GearViewModel by viewModels()

    private lateinit var mapViewStatic: MapView
    private lateinit var chartPace: BarChart
    private lateinit var tvMapDistanceChip: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvAvgPace: TextView
    private lateinit var tvTotalCalories: TextView
    private lateinit var tvElevation: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupChrome(view)

        val state = TrackingRepository.state.value
        populateSummary(state)
        setupGearSpinner(view.findViewById(R.id.spinnerGear), state)
        setupMap(state)
        setupPaceChart(state)
        setupShare(view, state)
    }

    private fun bindViews(view: View) {
        mapViewStatic = view.findViewById(R.id.mapViewStatic)
        chartPace = view.findViewById(R.id.chartPace)
        tvMapDistanceChip = view.findViewById(R.id.tvMapDistanceChip)
        tvTotalDuration = view.findViewById(R.id.tvTotalDuration)
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance)
        tvAvgPace = view.findViewById(R.id.tvAvgPace)
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories)
        tvElevation = view.findViewById(R.id.tvElevation)
    }

    private fun setupChrome(view: View) {
        view.findViewById<View>(R.id.btnMenuPostRun).setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        view.findViewById<View>(R.id.btnSettingsPostRun).setOnClickListener { }
        view.findViewById<View>(R.id.navHomePostRun).setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        view.findViewById<View>(R.id.navProfilePostRun).setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    private fun populateSummary(state: TrackingState) {
        tvTotalDuration.text = state.formattedDuration
        tvTotalDistance.text = String.format(Locale.getDefault(), "%.2f", state.distanceMeters / 1000f)
        tvAvgPace.text = state.formattedPace
        tvMapDistanceChip.text = state.formattedDistance.uppercase(Locale.getDefault())
        tvElevation.text = "0"
    }

    private fun setupGearSpinner(spinnerGear: Spinner, state: TrackingState) {
        var selectedGearId: Long? = null
        val btnSaveActivity = requireView().findViewById<View>(R.id.btnSaveActivity)
        btnSaveActivity.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            gearViewModel.gearList.first().let { gearList ->
                val items = listOf("Nessuna") + gearList.map { it.name }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    items
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                spinnerGear.adapter = adapter
                spinnerGear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        selectedGearId = if (pos == 0) null else gearList[pos - 1].id
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        selectedGearId = null
                    }
                }

                val prefs = UserPreferencesRepository(requireContext()).userPreferencesFlow.first()
                val weightKg = prefs.weightKg
                val calories = calculateCalories(state, weightKg)
                tvTotalCalories.text = String.format(Locale.getDefault(), "%.0f", calories)
                btnSaveActivity.isEnabled = true

                btnSaveActivity.setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
                        withContext(Dispatchers.IO) {
                            val db = AppDatabase.getInstance(requireContext())
                            val repo = ActivityRepository(db)
                            repo.saveActivity(
                                userId = uid,
                                sportType = state.sportType,
                                durationSeconds = state.elapsedSeconds,
                                distanceMeters = state.distanceMeters,
                                pathPoints = state.pathPoints,
                                weightKg = weightKg,
                                gearId = selectedGearId
                            )
                            selectedGearId?.let { gearId ->
                                db.gearDao().addKm(gearId, state.distanceMeters / 1000f)
                            }
                        }
                        TrackingRepository.resetState()
                        findNavController().navigate(R.id.action_post_run_to_home)
                    }
                }
            }
        }
    }

    private fun setupMap(state: TrackingState) {
        mapViewStatic.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            if (state.pathPoints.isEmpty()) return@loadStyleUri

            val points = state.pathPoints.map { Point.fromLngLat(it.lon, it.lat) }
            val annotationManager = mapViewStatic.annotations.createPolylineAnnotationManager()
            val polylineAnnotationOptions = PolylineAnnotationOptions()
                .withPoints(points)
                .withLineColor("#00E3FD")
                .withLineWidth(5.0)

            annotationManager.create(polylineAnnotationOptions)

            val centerPoint = points[points.size / 2]
            mapViewStatic.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(centerPoint)
                    .zoom(14.0)
                    .build()
            )
        }
    }

    private fun setupPaceChart(state: TrackingState) {
        val entries = buildSpeedEntries(state)
        if (entries.isEmpty()) {
            chartPace.setNoDataText("No route data")
            chartPace.invalidate()
            return
        }

        val cyan = ContextCompat.getColor(requireContext(), R.color.kinetic_secondary)
        val axisText = ContextCompat.getColor(requireContext(), R.color.kinetic_on_surface_variant)
        val grid = ContextCompat.getColor(requireContext(), R.color.kinetic_outline_variant)

        val dataSet = BarDataSet(entries, "Speed").apply {
            color = cyan
            valueTextSize = 0f
        }

        chartPace.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            setFitBars(true)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            axisLeft.apply {
                textColor = axisText
                axisMinimum = 0f
                gridColor = grid
            }
            setTouchEnabled(false)
            setDrawGridBackground(false)
            setDrawBorders(false)
            setNoDataTextColor(Color.WHITE)
            animateY(800)
            invalidate()
        }
    }

    private fun setupShare(view: View, state: TrackingState) {
        view.findViewById<View>(R.id.btnShareResult).setOnClickListener {
            val shareText = buildString {
                append("UniRun summary\n")
                append("Distance: ${state.formattedDistance}\n")
                append("Duration: ${state.formattedDuration}\n")
                append("Avg pace: ${state.formattedPace}\n")
                append("Calories: ${tvTotalCalories.text} kcal")
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(Intent.createChooser(intent, "Share result"))
            } else {
                Toast.makeText(requireContext(), "Share coming soon!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateCalories(state: TrackingState, weightKg: Float): Float {
        val met = when (state.sportType) {
            "RUN" -> 9.8f
            "WALK" -> 3.5f
            "BIKE" -> 7.5f
            else -> 7.5f
        }
        return met * weightKg * (state.elapsedSeconds / 3600f)
    }

    private fun buildSpeedEntries(state: TrackingState): List<BarEntry> {
        val points = state.pathPoints
        if (points.size < 2 || state.elapsedSeconds <= 0L) return emptyList()

        val segmentCount = min(8, points.size - 1)
        val secondsPerChunk = state.elapsedSeconds.toFloat() / segmentCount
        val entries = mutableListOf<BarEntry>()

        for (chunk in 0 until segmentCount) {
            val startIndex = (chunk * (points.size - 1)) / segmentCount
            val endIndex = maxOf(startIndex + 1, ((chunk + 1) * (points.size - 1)) / segmentCount)
            var distanceMeters = 0f

            for (index in startIndex + 1..endIndex) {
                distanceMeters += haversineMeters(points[index - 1], points[index])
            }

            val speedKmh = if (secondsPerChunk > 0f) {
                (distanceMeters / 1000f) / (secondsPerChunk / 3600f)
            } else {
                0f
            }

            entries += BarEntry(chunk.toFloat(), speedKmh)
        }

        return entries
    }

    private fun haversineMeters(from: LatLng, to: LatLng): Float {
        val radius = 6371000.0
        val dLat = Math.toRadians(to.lat - from.lat)
        val dLon = Math.toRadians(to.lon - from.lon)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(from.lat)) *
            cos(Math.toRadians(to.lat)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (radius * c).toFloat()
    }
}
