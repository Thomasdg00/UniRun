package com.univpm.unirun.ui.sport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.univpm.unirun.ui.components.KineticButton
import com.univpm.unirun.ui.theme.KineticTheme

@Composable
fun SportSelectionScreen(onSelect: (String) -> Unit) {
    KineticTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Seleziona Sport",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(32.dp))
            KineticButton(onClick = { onSelect("RUN") }, text = "🏃 Corsa")
            Spacer(modifier = Modifier.height(16.dp))
            KineticButton(onClick = { onSelect("WALK") }, text = "🚶 Camminata")
            Spacer(modifier = Modifier.height(16.dp))
            KineticButton(onClick = { onSelect("BIKE") }, text = "🚴 Bici")
        }
    }
}

@Composable
fun SportSelectionRoute(navController: NavController) {
    SportSelectionScreen { sport ->
        navController.navigate("tracking/$sport")
    }
}
