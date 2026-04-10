package com.univpm.unirun.ui.postrun

import android.content.Intent
import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.data.db.GearEntity
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import com.univpm.unirun.data.repository.ActivityRepository
import com.univpm.unirun.data.repository.LatLng
import com.univpm.unirun.data.repository.TrackingRepository
import com.univpm.unirun.data.repository.TrackingState
import com.univpm.unirun.ui.components.StatCard
import com.univpm.unirun.ui.components.UniRunButton
import com.univpm.unirun.ui.components.UniRunButtonVariant
import com.univpm.unirun.viewmodel.GearViewModel
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

@Composable
fun PostRunRoute(
    gearViewModel: GearViewModel,
    onNavigateHome: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gearList by gearViewModel.gearList.collectAsStateWithLifecycle(initialValue = emptyList())
    val state = remember { TrackingRepository.state.value }
    var selectedGearId by remember { mutableLongStateOf(0L) }
    var weightKg by remember { mutableFloatStateOf(70f) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        weightKg = UserPreferencesRepository(context).userPreferencesFlow.first().weightKg
    }

    PostRunScreen(
        trackingState = state,
        gearList = gearList,
        selectedGearId = selectedGearId.takeIf { it != 0L },
        weightKg = weightKg,
        isSaving = isSaving,
        onSelectedGearChange = { selectedGearId = it ?: 0L },
        onSaveClick = {
            if (!isSaving) {
                isSaving = true
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
                        val db = AppDatabase.getInstance(context)
                        ActivityRepository(db).saveActivity(
                            userId = uid,
                            sportType = state.sportType,
                            durationSeconds = state.elapsedSeconds,
                            distanceMeters = state.distanceMeters,
                            pathPoints = state.pathPoints,
                            weightKg = weightKg,
                            gearId = selectedGearId.takeIf { it != 0L }
                        )
                        selectedGearId.takeIf { it != 0L }?.let { gearId ->
                            db.gearDao().addKm(gearId, state.distanceMeters / 1000f)
                        }
                    }
                    TrackingRepository.resetState()
                    onNavigateHome()
                }
            }
        },
        onShareClick = {
            val shareText = buildString {
                append("UniRun summary\n")
                append("Distance: ${state.formattedDistance}\n")
                append("Duration: ${state.formattedDuration}\n")
                append("Avg pace: ${state.formattedPace}\n")
                append("Calories: ${"%.0f".format(calculateCalories(state, weightKg))} kcal")
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, "Share result"))
        },
        onProfileClick = onNavigateProfile,
        onHomeClick = onNavigateHome
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PostRunScreen(
    trackingState: TrackingState,
    gearList: List<GearEntity>,
    selectedGearId: Long?,
    weightKg: Float,
    isSaving: Boolean,
    onSelectedGearChange: (Long?) -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val calories = calculateCalories(trackingState, weightKg)
    var gearMenuExpanded by remember { mutableStateOf(false) }
    val selectedGearName = gearList.firstOrNull { it.id == selectedGearId }?.name ?: "Nessuna"

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "SESSION SUMMARY",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Text(
                    text = "Riepilogo finale in Compose con salvataggio attività, condivisione e associazione attrezzatura.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Duration",
                        value = trackingState.formattedDuration,
                        subtitle = "Elapsed time",
                        modifier = Modifier.fillMaxWidth()
                    )
                    StatCard(
                        title = "Distance",
                        value = trackingState.formattedDistance,
                        subtitle = trackingState.sportType,
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = MaterialTheme.colorScheme.secondary
                    )
                    StatCard(
                        title = "Pace",
                        value = trackingState.formattedPace,
                        subtitle = "min/km",
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                    StatCard(
                        title = "Calories",
                        value = String.format(Locale.getDefault(), "%.0f kcal", calories),
                        subtitle = "Estimated",
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = gearMenuExpanded,
                    onExpandedChange = { gearMenuExpanded = !gearMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedGearName,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        label = { Text("Attrezzatura") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gearMenuExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = gearMenuExpanded,
                        onDismissRequest = { gearMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nessuna") },
                            onClick = {
                                onSelectedGearChange(null)
                                gearMenuExpanded = false
                            }
                        )
                        gearList.forEach { gear ->
                            DropdownMenuItem(
                                text = { Text(gear.name) },
                                onClick = {
                                    onSelectedGearChange(gear.id)
                                    gearMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                                if (trackingState.pathPoints.isEmpty()) return@loadStyleUri
                                val points = trackingState.pathPoints.map { Point.fromLngLat(it.lon, it.lat) }
                                val annotationManager = annotations.createPolylineAnnotationManager()
                                annotationManager.create(
                                    PolylineAnnotationOptions()
                                        .withPoints(points)
                                        .withLineColor("#00E3FD")
                                        .withLineWidth(5.0)
                                )
                                mapboxMap.setCamera(
                                    CameraOptions.Builder()
                                        .center(points[points.size / 2])
                                        .zoom(14.0)
                                        .build()
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }

            item {
                AndroidView(
                    factory = { context -> BarChart(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) { chart ->
                    val entries = buildSpeedEntries(trackingState)
                    val dataSet = BarDataSet(entries, "Speed").apply {
                        color = android.graphics.Color.parseColor("#00E3FD")
                        valueTextSize = 0f
                    }
                    chart.apply {
                        data = BarData(dataSet).apply { barWidth = 0.6f }
                        description.isEnabled = false
                        legend.isEnabled = false
                        axisRight.isEnabled = false
                        xAxis.isEnabled = false
                        axisLeft.apply {
                            textColor = Color.WHITE
                            axisMinimum = 0f
                            gridColor = android.graphics.Color.parseColor("#33434759")
                        }
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        setTouchEnabled(false)
                        setNoDataTextColor(Color.WHITE)
                        invalidate()
                    }
                }
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UniRunButton(
                        text = if (isSaving) "SALVATAGGIO" else "SALVA ATTIVITA",
                        onClick = onSaveClick,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    UniRunButton(
                        text = "CONDIVIDI RISULTATO",
                        onClick = onShareClick,
                        variant = UniRunButtonVariant.Surface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    UniRunButton(
                        text = "VAI AL PROFILO",
                        onClick = onProfileClick,
                        variant = UniRunButtonVariant.Surface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    UniRunButton(
                        text = "TORNA ALLA HOME",
                        onClick = onHomeClick,
                        variant = UniRunButtonVariant.Surface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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
