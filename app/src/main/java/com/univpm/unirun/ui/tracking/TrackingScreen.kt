package com.univpm.unirun.ui.tracking

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.univpm.unirun.data.repository.TrackingState
import com.univpm.unirun.data.repository.TrackingStatus
import com.univpm.unirun.ui.theme.KineticTheme
import java.util.Locale

@Composable
fun TrackingScreen(
    trackingState: TrackingState,
    sportType: String,
    onSportTypeChange: (String) -> Unit,
    onPauseResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onShowStopDialog: (show: Boolean) -> Unit,
    showStopDialog: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    KineticTheme {
        Box(modifier = modifier.fillMaxSize()) {
            // Map container
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this
                        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                            val manager = annotations.createPolylineAnnotationManager()
                            if (trackingState.pathPoints.isNotEmpty()) {
                                val points = trackingState.pathPoints.map { 
                                    Point.fromLngLat(it.lon, it.lat) 
                                }
                                val options = PolylineAnnotationOptions()
                                    .withPoints(points)
                                    .withLineColor("#00E3FD")
                                    .withLineWidth(4.0)
                                manager.create(options)
                            }

                            trackingState.currentLatLng?.let { latLng ->
                                mapboxMap.setCamera(
                                    CameraOptions.Builder()
                                        .center(Point.fromLngLat(latLng.lon, latLng.lat))
                                        .zoom(15.0)
                                        .build()
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top bar with menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            // Dashboard overlay (bottom)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                // Sport selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Sport menu would open here
                        }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (sportType) {
                            "RUN" -> "🏃"
                            "WALK" -> "🚶"
                            "BIKE" -> "🚴"
                            else -> "🏃"
                        },
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = when (sportType) {
                            "RUN" -> "Corsa"
                            "WALK" -> "Camminata"
                            "BIKE" -> "Bici"
                            else -> "Corsa"
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (trackingState.currentLatLng != null) "GPS ACTIVE" else "GPS SEARCH",
                        color = if (trackingState.currentLatLng != null) Color.Green else Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox(
                        label = "Duration",
                        value = trackingState.formattedDuration,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        label = "Distance",
                        value = String.format(Locale.getDefault(), "%.2f km", trackingState.distanceMeters / 1000f),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        label = "Pace",
                        value = trackingState.formattedPace,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox(
                        label = "HR",
                        value = "--",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        label = "Elevation",
                        value = "--",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        label = "Calories",
                        value = String.format(Locale.getDefault(), "%.0f kcal", 
                            calculateCalories(trackingState)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Control buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onPauseResumeClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        enabled = trackingState.status != TrackingStatus.IDLE
                    ) {
                        Text(
                            when (trackingState.status) {
                                TrackingStatus.PAUSED -> "RESUME"
                                TrackingStatus.RUNNING -> "PAUSE"
                                TrackingStatus.IDLE -> "PAUSE"
                            }
                        )
                    }
                    Button(
                        onClick = { onShowStopDialog(true) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("STOP")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation row
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onHomeClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("🏠 Home")
                    }
                    OutlinedButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("👤 Profile")
                    }
                }
            }

            // Stop confirmation dialog
            if (showStopDialog) {
                AlertDialog(
                    onDismissRequest = { onShowStopDialog(false) },
                    title = { Text("Terminare allenamento?") },
                    text = { Text("Vuoi terminare e salvare l'allenamento?") },
                    confirmButton = {
                        TextButton(onClick = {
                            onShowStopDialog(false)
                            onStopClick()
                        }) {
                            Text("Sì")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { onShowStopDialog(false) }) {
                            Text("Annulla")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun calculateCalories(state: TrackingState, weightKg: Float = 70f): Float {
    val met = when (state.sportType) {
        "RUN" -> 9.8f
        "WALK" -> 3.5f
        "BIKE" -> 7.5f
        else -> 7.5f
    }
    return met * weightKg * (state.elapsedSeconds / 3600f)
}
