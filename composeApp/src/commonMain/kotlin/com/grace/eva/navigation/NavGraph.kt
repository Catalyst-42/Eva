package com.grace.eva.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grace.eva.di.AppContainer
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.presentation.screen.ActivitiesScreen
import com.grace.eva.presentation.screen.SettingsScreen
import com.grace.eva.presentation.screen.StatsScreen
import com.grace.eva.presentation.screen.TrackerScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    modifier: Modifier,
    navController: NavHostController = rememberNavController(),
    appContainer: AppContainer
) {
    val animationDuration = 300

    val forwardEnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(animationDuration)
    )

    val forwardExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(animationDuration)
    )

    val backEnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(animationDuration)
    )

    val backExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(animationDuration)
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Tracker.route,
        modifier = modifier,
        enterTransition = {
            val initialState = initialState.destination.route
            val targetState = targetState.destination.route

            val initialOrder = Screen.entries.first { it.route == initialState }.order
            val targetOrder = Screen.entries.first { it.route == targetState }.order

            if (targetOrder > initialOrder) {
                forwardEnterTransition
            } else {
                backEnterTransition
            }
        },
        exitTransition = {
            val initialState = initialState.destination.route
            val targetState = targetState.destination.route

            val initialOrder = Screen.entries.first { it.route == initialState }.order
            val targetOrder = Screen.entries.first { it.route == targetState }.order

            if (targetOrder > initialOrder) {
                forwardExitTransition
            } else {
                backExitTransition
            }
        }) {
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