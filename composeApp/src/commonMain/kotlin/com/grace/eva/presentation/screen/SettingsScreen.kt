package com.grace.eva.presentation.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.presentation.viewmodel.TrackerViewModel


@Composable
fun SettingsScreen(
    appContainer: AppContainer
) {
    val viewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModel.Factory(appContainer)
    )

    SettingsScreenContent(viewModel)
}

@Composable
fun SettingsScreenContent(viewModel: TrackerViewModel) {
    Text("Settings")
}
