package com.univpm.unirun.ui.gear

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.univpm.unirun.data.db.GearEntity
import com.univpm.unirun.viewmodel.GearViewModel

@Composable
fun GearRoute(
    gearViewModel: GearViewModel,
    modifier: Modifier = Modifier
) {
    val gearList by gearViewModel.gearList.collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<GearEntity?>(null) }

    GearScreen(
        gearList = gearList,
        onAddClick = { showAddDialog = true },
        onDeleteClick = { gear -> deleteTarget = gear },
        modifier = modifier
    )

    if (deleteTarget != null) {
        val gear = deleteTarget!!
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Conferma eliminazione") },
            text = { Text("Vuoi davvero eliminare ${gear.name}?") },
            confirmButton = {
                Button(onClick = {
                    gearViewModel.deleteGear(gear)
                    deleteTarget = null
                }) { Text("Elimina") }
            },
            dismissButton = {
                Button(onClick = { deleteTarget = null }) { Text("Annulla") }
            }
        )
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var typeExpanded by remember { mutableStateOf(false) }
        var selectedType by remember { mutableStateOf("Scarpe") }
        var thresholdStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nuova Attrezzatura") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        Button(onClick = { typeExpanded = true }) { Text(selectedType) }
                        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            DropdownMenuItem(text = { Text("Scarpe") }, onClick = { selectedType = "Scarpe"; typeExpanded = false })
                            DropdownMenuItem(text = { Text("Bici") }, onClick = { selectedType = "Bici"; typeExpanded = false })
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = thresholdStr, onValueChange = { thresholdStr = it }, label = { Text("Soglia km") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val default = if (selectedType == "Scarpe") 500f else 3000f
                    val limit = thresholdStr.toFloatOrNull() ?: default
                    if (name.isNotBlank()) {
                        gearViewModel.addGear(name.trim(), selectedType, limit)
                    }
                    showAddDialog = false
                }) { Text("Aggiungi") }
            },
            dismissButton = {
                Button(onClick = { showAddDialog = false }) { Text("Annulla") }
            }
        )
    }
}
