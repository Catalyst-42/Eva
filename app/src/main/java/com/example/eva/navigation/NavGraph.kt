package com.example.eva.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eva.di.AppContainer
import com.example.eva.presentation.screen.ActivityScreen
import com.example.eva.presentation.screen.TrackerScreen

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
        modifier = modifier
    ) {
        composable(Screen.Tracker.route) {
            TrackerScreen(appContainer)
        }

        composable(Screen.Activities.route) {
            ActivityScreen(appContainer)
        }

        composable(Screen.Stats.route) {
            // TODO: Stats screen
            Text("Stats")
        }

        composable(Screen.Settings.route) {
            // TODO: Settings screen
            Text("Settings")
        }
    }
}