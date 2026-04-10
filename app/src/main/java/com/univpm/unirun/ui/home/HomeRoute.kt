package com.univpm.unirun.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.univpm.unirun.viewmodel.HomeViewModel

@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val weeklyKm by homeViewModel.weeklyKm.collectAsStateWithLifecycle()
    val activeTimeFormatted by homeViewModel.activeTimeFormatted.collectAsStateWithLifecycle()
    val activeTimeDelta by homeViewModel.activeTimeDelta.collectAsStateWithLifecycle()
    val intensityKcal by homeViewModel.intensityKcal.collectAsStateWithLifecycle()
    val streak by homeViewModel.streak.collectAsStateWithLifecycle()
    val recoveryStatus by homeViewModel.recoveryStatus.collectAsStateWithLifecycle()
    val recentActivities by homeViewModel.recentActivities.collectAsStateWithLifecycle()

    HomeScreen(
        weeklyKm = weeklyKm,
        activeTimeFormatted = activeTimeFormatted,
        activeTimeDelta = activeTimeDelta,
        intensityKcal = intensityKcal,
        streak = streak,
        recoveryStatus = recoveryStatus,
        recentActivities = recentActivities,
        onMenuClick = {
            // Handle menu click; e.g., open drawer or show menu
        },
        onSettingsClick = {
            navController.navigate("settings")
        },
        onStartActivityClick = {
            navController.navigate("sport_selection")
        },
        onActivityClick = { activity ->
            navController.navigate("detail/${activity.id}")
        },
        modifier = modifier
    )
}
