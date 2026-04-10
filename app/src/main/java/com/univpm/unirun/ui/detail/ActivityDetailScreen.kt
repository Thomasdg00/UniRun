package com.univpm.unirun.ui.detail

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.ui.theme.KineticTheme
import kotlin.math.pow
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityDetailScreen(
    activity: ActivityEntity?,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    KineticTheme {
        if (activity == null) {
            Column(
                modifier = modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Activity not found", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onBackClick, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Back")
                }
            }
            return@KineticTheme
        }

        LazyColumn(modifier = modifier.fillMaxSize()) {
            // Header
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    val sportIcon = when (activity.sportType) {
                        "RUN" -> "🏃"
                        "WALK" -> "🚶"
                        "BIKE" -> "🚴"
                        else -> "🏃"
                    }
                    val sportName = when (activity.sportType) {
                        "RUN" -> "Corsa"
                        "WALK" -> "Camminata"
                        "BIKE" -> "Bicicletta"
                        else -> activity.sportType
                    }
                    Text(sportIcon, fontSize = 48.sp)
                    Text(sportName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    Text(
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(activity.startTimestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Map
            item {
                AndroidView(
                    factory = { context -> MapView(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) { mapView ->
                    val points = parsePolylineJson(activity.polylineJson)
                    if (points.isNotEmpty()) {
                        val mapboxPoints = points.map { Point.fromLngLat(it.second, it.first) }
                        mapView.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                            val annotationManager = mapView.annotations.createPolylineAnnotationManager()
                            val polylineAnnotationOptions = PolylineAnnotationOptions()
                                .withPoints(mapboxPoints)
                                .withLineColor("#EE4B2B")
                                .withLineWidth(5.0)
                            annotationManager.create(polylineAnnotationOptions)
                            val centerPoint = mapboxPoints[mapboxPoints.size / 2]
                            mapView.mapboxMap.setCamera(
                                CameraOptions.Builder()
                                    .center(centerPoint)
                                    .zoom(14.0)
                                    .build()
                            )
                        }
                    }
                }
            }

            // Metrics Grid
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    val distanceKm = activity.distanceMeters / 1000f
                    val paceSeconds = if (distanceKm > 0) (activity.durationSeconds / distanceKm).toInt() else 0
                    val speedKmh = if (activity.durationSeconds > 0) distanceKm / (activity.durationSeconds / 3600f) else 0f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "Distance",
                            value = if (activity.distanceMeters < 1000) String.format(Locale.getDefault(), "%.0f m", activity.distanceMeters) else String.format(Locale.getDefault(), "%.2f km", distanceKm),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Duration",
                            value = "%d:%02d".format(activity.durationSeconds / 60, activity.durationSeconds % 60),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "Pace",
                            value = "%d:%02d min/km".format(paceSeconds / 60, paceSeconds % 60),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Speed",
                            value = String.format(Locale.getDefault(), "%.1f km/h", speedKmh),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "Calories",
                            value = String.format(Locale.getDefault(), "%.0f kcal", activity.calories),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Speed Chart
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Speed Profile", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                    AndroidView(
                        factory = { context -> LineChart(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 8.dp)
                    ) { chart ->
                        val points = parsePolylineJson(activity.polylineJson)
                        if (points.size > 1) {
                            val timePerSegmentSec = activity.durationSeconds.toFloat() / (points.size - 1)
                            val entries = mutableListOf<Entry>()
                            for (i in 1 until points.size) {
                                val distM = haversineMeters(points[i - 1], points[i])
                                val speedKmh = (distM / 1000f) / (timePerSegmentSec / 3600f)
                                val clampedSpeed = speedKmh.coerceIn(0f, 50f)
                                entries.add(Entry(i.toFloat(), clampedSpeed))
                            }
                            val dataSet = LineDataSet(entries, "Speed (km/h)").apply {
                                color = Color.parseColor("#EE4B2B")
                                setDrawCircles(false)
                                lineWidth = 2f
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                                setDrawFilled(true)
                                fillColor = Color.parseColor("#33EE4B2B")
                                valueTextSize = 0f
                            }
                            chart.apply {
                                data = LineData(dataSet)
                                description.isEnabled = false
                                legend.isEnabled = false
                                xAxis.isEnabled = false
                                axisRight.isEnabled = false
                                axisLeft.apply {
                                    textColor = Color.WHITE
                                    axisMinimum = 0f
                                    gridColor = Color.parseColor("#33888888")
                                }
                                setTouchEnabled(false)
                                animateX(800)
                                invalidate()
                            }
                        }
                    }
                }
            }

            // Delete Button
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DELETE ACTIVITY")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
        }
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
    } catch (e: Exception) {
    }
    return result
}

private fun haversineMeters(from: Pair<Double, Double>, to: Pair<Double, Double>): Float {
    val R = 6371000.0
    val dLat = Math.toRadians(to.first - from.first)
    val dLon = Math.toRadians(to.second - from.second)
    val a = Math.sin(dLat / 2).pow(2) +
            Math.cos(Math.toRadians(from.first)) *
            Math.cos(Math.toRadians(to.first)) *
            Math.sin(dLon / 2).pow(2)
    return (2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))).toFloat()
}
