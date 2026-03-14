package com.grace.eva.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.domain.model.Activities
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
        viewModel = viewModel,
        activities = viewModel.uiState.collectAsStateWithLifecycle().value.activities
    )
}

@Composable
fun SettingsScreenContent(
    viewModel: TrackerViewModel,
    activities: Activities
) {
    var editedName by remember(activities.name) { mutableStateOf(activities.name) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ActivitiesCard(
            activities = activities,
            onNameChange = {
                editedName = it // TODO: Сохранять новое название в ViewModel
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Управление сохранениями",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = { /* TODO: Сменить сохранение */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сменить сохранение")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.onSaveActivities() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Записать текущее сохранение")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* TODO: Завершить сохранение */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Завершить сохранение")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Импорт / Экспорт",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = { /* TODO: Импорт */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Импортировать файл сохранения")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* TODO: Экспорт */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Экспортировать файл сохранения")
        }
    }
}