package com.univpm.unirun.ui.gear

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.univpm.unirun.data.db.GearEntity
import com.univpm.unirun.ui.components.KineticButton
import com.univpm.unirun.ui.theme.KineticTheme

@Composable
fun GearScreen(
    gearList: List<GearEntity>,
    onAddClick: () -> Unit,
    onDeleteClick: (GearEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    KineticTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = onAddClick) {
                    Text("+")
                }
            }
        ) { innerPadding ->
            Box(modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                if (gearList.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nessuna attrezzatura", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))
                        KineticButton(onClick = onAddClick, text = "Aggiungi Attrezzatura")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                        items(gearList, key = { it.id }) { gear ->
                            GearRow(gear = gear, onDelete = { onDeleteClick(gear) })
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GearRow(gear: GearEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = gear.name, style = MaterialTheme.typography.titleMedium)
            Text(text = if (gear.type == "Scarpe") "👟 Scarpe" else "🚴 Bici", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(progress = (gear.totalKm / gear.wearThresholdKm).coerceIn(0f, 1f), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "%.1f / %.0f km".format(gear.totalKm, gear.wearThresholdKm), style = MaterialTheme.typography.bodySmall)
            if (gear.totalKm >= gear.wearThresholdKm * 0.9f) {
                Text(text = "⚠️ Usura elevata", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Elimina")
        }
    }
}
