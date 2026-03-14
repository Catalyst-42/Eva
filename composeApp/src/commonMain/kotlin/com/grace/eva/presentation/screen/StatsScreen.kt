package com.grace.eva.presentation.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.presentation.viewmodel.TrackerViewModel


@Composable
fun StatsScreen(
    appContainer: AppContainer
) {
    val viewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModel.Factory(appContainer)
    )

    StatsScreenContent(viewModel)
}

@Composable
fun StatsScreenContent(viewModel: TrackerViewModel) {
    Text("Stats", modifier = Modifier.fillMaxWidth())
}
