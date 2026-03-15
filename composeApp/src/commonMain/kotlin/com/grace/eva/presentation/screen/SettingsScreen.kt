package com.grace.eva.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.presentation.component.ActivitiesCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel

@Composable
fun SettingsScreen(
    appContainer: AppContainer
) {
    val viewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModel.Factory(appContainer)
    )

    SettingsScreenContent(
        viewModel = viewModel
    )
}

@Composable
fun SettingsScreenContent(
    viewModel: TrackerViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val activities = state.activities

    var editedName by remember(activities.name) { mutableStateOf(activities.name) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        ActivitiesCard(
            activities = activities,
            viewModel = viewModel, // Just pass the whole ViewModel
            onNameChange = { newName ->
                editedName = newName
                // TODO: Implement name change
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // TODO: Implement import
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Импортировать файл сохранения")
        }
    }
}