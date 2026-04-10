package com.univpm.unirun.ui.water

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.univpm.unirun.viewmodel.WaterViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Composable
fun WaterScreen(consumed: Int, goal: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Obiettivo: $goal ml")
        Text(text = "$consumed ml")
        val percentage = if (goal > 0) (consumed * 100 / goal).coerceAtMost(100) else 0
        Text(text = "$percentage%")
        CircularProgressIndicator(progress = if (goal>0) consumed.toFloat()/goal else 0f, modifier = Modifier.padding(16.dp))
        Button(onClick = onAdd) { Text("+250 ml") }
        Button(onClick = onRemove) { Text("-250 ml") }
    }
}

@Composable
fun WaterRoute(viewModel: WaterViewModel) {
    val consumed by viewModel.consumedMl.collectAsStateWithLifecycle(initialValue = 0)
    val goal by viewModel.dailyGoalMl.collectAsStateWithLifecycle(initialValue = 0)
    WaterScreen(consumed, goal, { viewModel.addWater(250) }, { viewModel.removeWater(250) })
}
