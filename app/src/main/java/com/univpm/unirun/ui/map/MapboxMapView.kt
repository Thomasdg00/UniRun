package com.univpm.unirun.ui.map

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager

@Composable
fun MapboxMapView(modifier: Modifier = Modifier, pathPoints: List<Point> = emptyList(), onMapReady: (MapboxMap) -> Unit = {}) {
    AndroidView(factory = { context: Context ->
        MapView(context).apply {
            getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
                onMapReady(getMapboxMap())
            }
        }
    }, update = { mapView ->
        // TODO: update polyline when pathPoints changes
    }, modifier = modifier)
}
