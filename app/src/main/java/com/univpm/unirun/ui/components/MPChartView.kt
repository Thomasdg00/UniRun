package com.univpm.unirun.ui.components

import android.content.Context
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun LineChartView(modifier: Modifier = Modifier, points: List<Float> = emptyList()) {
    AndroidView(factory = { context: Context ->
        LineChart(context).apply {
            description.isEnabled = false
            legend.isEnabled = false
        }
    }, update = { chart ->
        val entries = points.mapIndexed { i, v -> Entry(i.toFloat(), v) }
        val dataSet = LineDataSet(entries, "")
        chart.data = LineData(dataSet)
        chart.invalidate()
    }, modifier = modifier)
}
