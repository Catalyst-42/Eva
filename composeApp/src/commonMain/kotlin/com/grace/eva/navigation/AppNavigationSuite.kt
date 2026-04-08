package com.grace.eva.navigation

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.ui.theme.EvaTheme

@Composable
fun AppNavigationSuite(
    navController: NavHostController, onNavigate: (String) -> Unit, content: @Composable () -> Unit
) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Screen.entries.filter { it.showInNavigation }.forEach { screen ->
                item(
                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                    label = { Text(screen.label) },
                    selected = screen.route == currentRoute,
                    onClick = { onNavigate(screen.route) }
                )
            }
        }
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun AppNavigationSuitePreview() {
    EvaTheme {
        val mockContainer = MockAppContainer(MockType.SIMPLE)
        val navController = rememberNavController()

        AppNavigationSuite(
            navController = navController,
            onNavigate = {}
        ) {
            NavGraph(
                modifier = Modifier.statusBarsPadding(),
                navController = navController,
                appContainer = mockContainer
            )
        }
    }
}
