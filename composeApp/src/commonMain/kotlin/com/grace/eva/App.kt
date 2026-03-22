package com.grace.eva

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.grace.eva.di.AppContainer
import com.grace.eva.navigation.NavGraph
import com.grace.eva.navigation.AppNavigationSuite
import com.grace.eva.ui.theme.EvaTheme

@Composable
fun App(
    appContainer: AppContainer
) {
    EvaTheme {
        EvaApp(appContainer = appContainer)
    }
}

@Composable
fun EvaApp(
    appContainer: AppContainer
) {
    val navController = rememberNavController()

    AppNavigationSuite(
        navController = navController, onNavigate = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }) {
        NavGraph(
            modifier = Modifier.statusBarsPadding(),
            navController = navController,
            appContainer = appContainer
        )
    }
}