package com.univpm.unirun.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.univpm.unirun.R
import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class ActivityDetailFragment : Fragment(R.layout.fragment_activity_detail) {

    private lateinit var tvDetailSportIcon: TextView
    private lateinit var tvDetailSport: TextView
    private lateinit var tvDetailDate: TextView
    private lateinit var tvDetailDistance: TextView
    private lateinit var tvDetailDuration: TextView
    private lateinit var tvDetailPace: TextView
    private lateinit var tvDetailSpeed: TextView
    private lateinit var tvDetailCalories: TextView
    private lateinit var mapViewDetail: MapView
    private lateinit var chartSpeed: LineChart
    private lateinit var btnDeleteActivity: Button
    private lateinit var tvChartTitle: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDetailSportIcon = view.findViewById(R.id.tvDetailSportIcon)
        tvDetailSport = view.findViewById(R.id.tvDetailSport)
        tvDetailDate = view.findViewById(R.id.tvDetailDate)
        tvDetailDistance = view.findViewById(R.id.tvDetailDistance)
        tvDetailDuration = view.findViewById(R.id.tvDetailDuration)
        tvDetailPace = view.findViewById(R.id.tvDetailPace)
        tvDetailSpeed = view.findViewById(R.id.tvDetailSpeed)
        tvDetailCalories = view.findViewById(R.id.tvDetailCalories)
        mapViewDetail = view.findViewById(R.id.mapViewDetail)
        chartSpeed = view.findViewById(R.id.chartSpeed)
        btnDeleteActivity = view.findViewById(R.id.btnDeleteActivity)
        tvChartTitle = view.findViewById(R.id.tvChartTitle)

        val activityId = arguments?.getLong("activityId") ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val activity = withContext(Dispatchers.IO) { db.activityDao().getById(activityId) }
            if (activity == null) { findNavController().navigateUp(); return@launch }
            populateUI(activity)
        }

        btnDeleteActivity.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Elimina attività")
                .setMessage("Sei sicuro? L'operazione non può essere annullata.")
                .setPositiveButton("Elimina") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            val db = AppDatabase.getInstance(requireContext())
                            val activity = db.activityDao().getById(activityId)
                            activity?.let { db.activityDao().delete(it) }
                        }
                        findNavController().navigateUp()
                    }
                }
                .setNegativeButton("Annulla", null)
                .show()
        }
    }

    private fun populateUI(activity: ActivityEntity) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvDetailDate.text = sdf.format(Date(activity.startTimestamp))

        tvDetailSportIcon.text = when(activity.sportType) {
            "RUN"  -> "🏃"
            "WALK" -> "🚶"
            "BIKE" -> "🚴"
            else   -> "🏃"
        }
        
        tvDetailSport.text = when(activity.sportType) {
            "RUN" -> "Corsa"
            "WALK" -> "Camminata"
            "BIKE" -> "Bicicletta"
            else -> activity.sportType
        }

        tvDetailDistance.text = if (activity.distanceMeters < 1000) 
            "%.0f m".format(activity.distanceMeters)
        else 
            "%.2f km".format(activity.distanceMeters / 1000f)

        val min = activity.durationSeconds / 60
        val sec = activity.durationSeconds % 60
        tvDetailDuration.text = "%d:%02d".format(min, sec)

        tvDetailCalories.text = "%.0f kcal".format(activity.calories)

        val distanceKm = activity.distanceMeters / 1000f
        if (distanceKm > 0) {
            val paceSeconds = (activity.durationSeconds / distanceKm).toInt()
            val paceMin = paceSeconds / 60
            val paceSec = paceSeconds % 60
            tvDetailPace.text = "%d:%02d min/km".format(paceMin, paceSec)

            // ✅ PROTEZIONE: Verificare divisione per zero
            if (activity.durationSeconds > 0) {
                val speedKmh = distanceKm / (activity.durationSeconds / 3600f)
                tvDetailSpeed.text = "%.1f km/h".format(speedKmh)
            } else {
                tvDetailSpeed.text = "0.0 km/h"
            }
        } else {
            tvDetailPace.text = "0:00 min/km"
            tvDetailSpeed.text = "0.0 km/h"
        }

        drawMapPolyline(activity)
        setupSpeedChart(activity)
    }

    private fun drawMapPolyline(activity: ActivityEntity) {
        val points = parsePolylineJson(activity.polylineJson)
        if (points.isEmpty()) {
            mapViewDetail.visibility = View.GONE
            return
        }

        val mapboxPoints = points.map { Point.fromLngLat(it.second, it.first) }
        
        mapViewDetail.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            val annotationManager = mapViewDetail.annotations.createPolylineAnnotationManager()
            val polylineAnnotationOptions = PolylineAnnotationOptions()
                .withPoints(mapboxPoints)
                .withLineColor("#EE4B2B")
                .withLineWidth(5.0)

            annotationManager.create(polylineAnnotationOptions)

            val centerPoint = mapboxPoints[mapboxPoints.size / 2]
            mapViewDetail.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(centerPoint)
                    .zoom(14.0)
                    .build()
            )
        }
    }

    private fun setupSpeedChart(activity: ActivityEntity) {
        val points = parsePolylineJson(activity.polylineJson)
        if (points.size < 2) {
            chartSpeed.visibility = View.GONE
            tvChartTitle.visibility = View.GONE
            return
        }

        val timePerSegmentSec = activity.durationSeconds.toFloat() / (points.size - 1)
        val entries = mutableListOf<Entry>()

        for (i in 1 until points.size) {
            val distM = haversineMeters(points[i-1], points[i])
            val speedKmh = (distM / 1000f) / (timePerSegmentSec / 3600f)
            val clampedSpeed = speedKmh.coerceIn(0f, 50f)
            entries.add(Entry(i.toFloat(), clampedSpeed))
        }

        val dataSet = LineDataSet(entries, "Velocità (km/h)").apply {
            color = Color.parseColor("#EE4B2B")
            setDrawCircles(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#33EE4B2B")
            valueTextSize = 0f
        }

        chartSpeed.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.apply {
                textColor = if (isDarkMode()) Color.WHITE else Color.BLACK
                axisMinimum = 0f
                gridColor = Color.parseColor("#33888888")
            }
            setTouchEnabled(false)
            animateX(800)
            invalidate()
        }
    }

    private fun parsePolylineJson(json: String): List<Pair<Double, Double>> {
        val result = mutableListOf<Pair<Double, Double>>()
        if (json.isEmpty()) return result
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                result.add(Pair(obj.getDouble("lat"), obj.getDouble("lng")))
            }
        } catch (e: Exception) { }
        return result
    }

    private fun haversineMeters(from: Pair<Double, Double>, to: Pair<Double, Double>): Float {
        val R = 6371000.0
        val dLat = Math.toRadians(to.first - from.first)
        val dLon = Math.toRadians(to.second - from.second)
        val a = Math.sin(dLat/2).pow(2) +
                Math.cos(Math.toRadians(from.first)) *
                Math.cos(Math.toRadians(to.first)) *
                Math.sin(dLon/2).pow(2)
        return (2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))).toFloat()
    }

    private fun isDarkMode(): Boolean {
        return (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

}