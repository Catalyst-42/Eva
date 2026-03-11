package com.grace.eva

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.grace.eva.di.AppContainer
import com.grace.eva.navigation.NavGraph
import com.grace.eva.navigation.Screen
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
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Screen.entries.forEach { screen ->
                item(
                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                    label = { Text(screen.label) },
                    selected = screen.route == currentRoute,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        NavGraph(
            modifier = Modifier.statusBarsPadding(),
            navController = navController,
            appContainer = appContainer
        )
    }
}
