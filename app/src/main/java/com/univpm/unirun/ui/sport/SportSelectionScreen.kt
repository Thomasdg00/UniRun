package com.univpm.unirun.ui.sport

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SportSelectionScreen(onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { onSelect("RUN") }) { Text("Run") }
        Button(onClick = { onSelect("WALK") }) { Text("Walk") }
        Button(onClick = { onSelect("BIKE") }) { Text("Bike") }
    }
}

@Composable
fun SportSelectionRoute(navController: NavController) {
    SportSelectionScreen { sport ->
        navController.navigate("tracking/$sport")
    }
}
