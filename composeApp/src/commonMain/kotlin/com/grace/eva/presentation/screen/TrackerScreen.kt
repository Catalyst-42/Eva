package com.grace.eva.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel

@Composable
fun TrackerScreen(
    appContainer: AppContainer,
) {
    val viewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModel.Factory(appContainer)
    )

    TrackerScreenContent(viewModel)
}

@Composable
fun TrackerScreenContent(viewModel: TrackerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val currentSave = state.currentSave
    val activities = currentSave?.activities ?: emptyList()

    // TODO: Make ability to change this list content
    val activityTypes = listOf("Сон", "Отдых", "Пары", "Транспорт", "Домашка", "Другое")

    // Get the last activity - this is the current one
    val currentActivity = activities.lastOrNull()
    val isSaveActive = currentSave?.end == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Текущая активность",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (currentSave == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Нет активного сохранения",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            ActivityCard(
                activity = currentActivity,
                viewModel = viewModel,
                expanded = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSaveActive) "Переключить активность" else "Сохранение завершено",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        activityTypes.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { name ->
                    Button(
                        onClick = {
                            if (currentSave != null && isSaveActive) {
                                viewModel.onCreateActivity(name)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = currentSave != null && isSaveActive
                    ) {
                        Text(name)
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (currentSave == null) {
            Text(
                text = "Выберите сохранение в настройках",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else if (!isSaveActive) {
            Text(
                text = "Чтобы переключать этапы, продолжите сохранение в настройках",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackerScreen() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }

    TrackerScreenContent(mockViewModel)
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackerScreen_NoSave() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.EMPTY))
    }

    TrackerScreenContent(mockViewModel)
}