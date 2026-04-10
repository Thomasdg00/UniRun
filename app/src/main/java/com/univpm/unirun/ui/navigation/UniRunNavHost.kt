package com.univpm.unirun.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.ui.auth.AuthRoute
import com.univpm.unirun.ui.auth.RegisterRoute
import com.univpm.unirun.ui.detail.ActivityDetailRoute
import com.univpm.unirun.ui.gear.GearRoute
import com.univpm.unirun.ui.home.HomeRoute
import com.univpm.unirun.ui.onboarding.OnboardingRoute
import com.univpm.unirun.ui.postrun.PostRunRoute
import com.univpm.unirun.ui.profile.ProfileRoute
import com.univpm.unirun.ui.sport.SportSelectionRoute
import com.univpm.unirun.ui.tracking.TrackingRoute
import com.univpm.unirun.ui.water.WaterRoute
import com.univpm.unirun.viewmodel.AuthViewModel
import com.univpm.unirun.viewmodel.GearViewModel
import com.univpm.unirun.viewmodel.HomeViewModel
import com.univpm.unirun.viewmodel.ProfileViewModel
import com.univpm.unirun.viewmodel.TrackingViewModel
import com.univpm.unirun.viewmodel.WaterViewModel

@Composable
fun UniRunNavHost(
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthRoute(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterRoute(
                viewModel = authViewModel,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingRoute(
                viewModel = authViewModel,
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeRoute(homeViewModel = homeViewModel, navController = navController)
        }

        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            ProfileRoute(profileViewModel = profileViewModel, navController = navController)
        }

        composable(Screen.Water.route) {
            val waterViewModel: WaterViewModel = viewModel()
            WaterRoute(viewModel = waterViewModel)
        }

        composable(Screen.Gear.route) {
            val gearViewModel: GearViewModel = viewModel()
            GearRoute(gearViewModel = gearViewModel)
        }

        composable(Screen.SportSelection.route) {
            SportSelectionRoute(navController = navController)
        }

        composable(
            route = Screen.Tracking.route,
            arguments = listOf(navArgument("sportType") { type = NavType.StringType })
        ) { backStackEntry ->
            val trackingViewModel: TrackingViewModel = viewModel()
            TrackingRoute(
                trackingViewModel = trackingViewModel,
                navController = navController,
                initialSportType = backStackEntry.arguments?.getString("sportType") ?: "RUN"
            )
        }

        composable(Screen.PostRun.route) {
            val gearViewModel: GearViewModel = viewModel()
            PostRunRoute(
                gearViewModel = gearViewModel,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateProfile = {
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.ActivityDetail.route,
            arguments = listOf(navArgument("activityId") { type = NavType.LongType })
        ) { backStackEntry ->
            ActivityDetailRoute(
                activityId = backStackEntry.arguments?.getLong("activityId") ?: return@composable,
                appDatabase = AppDatabase.getInstance(context),
                navController = navController
            )
        }
    }
}
