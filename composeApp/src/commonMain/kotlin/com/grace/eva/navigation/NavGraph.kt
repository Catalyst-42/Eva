package com.grace.eva.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grace.eva.di.AppContainer
import com.grace.eva.presentation.screen.ActivitiesScreen
import com.grace.eva.presentation.screen.SettingsScreen
import com.grace.eva.presentation.screen.StatsScreen
import com.grace.eva.presentation.screen.TrackerScreen

enum class Screen(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    Tracker("tracker", Icons.Default.PlayArrow, "Трекер"),
    Activities("activities", Icons.Default.Menu, "Активности"),
    Stats("stats", Icons.Default.Info, "Статистика"),
    Settings("settings", Icons.Default.Settings, "Настройки")
}

@Composable
fun NavGraph(
    modifier: Modifier,
    navController: NavHostController = rememberNavController(),
    appContainer: AppContainer
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tracker.route,
        modifier = modifier,
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } }
    ) {
        composable(Screen.Tracker.route) {
            TrackerScreen(appContainer)
        }

        composable(Screen.Activities.route) {
            ActivitiesScreen(appContainer)
        }

        composable(Screen.Stats.route) {
            StatsScreen(appContainer)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(appContainer)
        }
    }
}