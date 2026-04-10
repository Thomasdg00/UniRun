package com.univpm.unirun.ui.navigation

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Register : Screen("register")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object Water : Screen("water")
    data object Gear : Screen("gear")
    data object SportSelection : Screen("sport_selection")
    data object PostRun : Screen("post_run")
    data object Tracking : Screen("tracking/{sportType}") {
        fun createRoute(sportType: String): String = "tracking/$sportType"
    }

    data object ActivityDetail : Screen("detail/{activityId}") {
        fun createRoute(activityId: Long): String = "detail/$activityId"
    }
}
