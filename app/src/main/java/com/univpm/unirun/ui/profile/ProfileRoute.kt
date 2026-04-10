package com.univpm.unirun.ui.profile

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.univpm.unirun.viewmodel.ProfileViewModel

@Composable
fun ProfileRoute(
    profileViewModel: ProfileViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val userName by profileViewModel.userName.collectAsStateWithLifecycle()
    val totalDistanceKm by profileViewModel.totalDistanceKm.collectAsStateWithLifecycle()
    val totalActivities by profileViewModel.totalActivities.collectAsStateWithLifecycle()
    val avgPaceFormatted by profileViewModel.avgPaceFormatted.collectAsStateWithLifecycle()
    val totalCalories by profileViewModel.totalCalories.collectAsStateWithLifecycle()
    val currentMonthLabel by profileViewModel.currentMonthLabel.collectAsStateWithLifecycle()
    val monthlyKm by profileViewModel.monthlyKm.collectAsStateWithLifecycle()
    val monthlyTimeFormatted by profileViewModel.monthlyTimeFormatted.collectAsStateWithLifecycle()
    val groupedHistory by profileViewModel.groupedHistory.collectAsStateWithLifecycle()
    val joinedLabel by profileViewModel.joinedLabel.collectAsStateWithLifecycle()
    val locationLabel by profileViewModel.locationLabel.collectAsStateWithLifecycle()

    var showEditSheet by remember { mutableStateOf(false) }

    ProfileScreen(
        userName = userName,
        totalDistanceKm = totalDistanceKm,
        totalActivities = totalActivities,
        avgPaceFormatted = avgPaceFormatted,
        totalCalories = totalCalories,
        currentMonthLabel = currentMonthLabel,
        monthlyKm = monthlyKm,
        monthlyTimeFormatted = monthlyTimeFormatted,
        groupedHistory = groupedHistory,
        joinedLabel = joinedLabel,
        locationLabel = locationLabel,
        onEditClick = { showEditSheet = true },
        onActivityClick = { activityId ->
            navController.navigate("detail/$activityId")
        },
        modifier = modifier
    )

    if (showEditSheet) {
        var nameInput by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showEditSheet = false },
            title = { Text("Edit Profile") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    // Save logic if applicable
                    showEditSheet = false
                }) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = { showEditSheet = false }) { Text("Cancel") }
            }
        )
    }
}
