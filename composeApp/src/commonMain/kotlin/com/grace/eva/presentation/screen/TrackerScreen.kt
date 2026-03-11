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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.domain.model.Activity
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.utils.formatDuration
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Instant

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
    //  TODO: Make ability to change this list content
    val activityTypes = listOf("Работа", "Учёба", "Отдых", "Спорт", "Еда", "Сон")

    var currentTime by remember { mutableStateOf(Clock.System.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            // Recompose timer
            currentTime = Clock.System.now()
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ActivityCard(
            activity = state.activities.activities.lastOrNull(),
            now = currentTime
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
        Text(text = "Переключить активность:")
        Spacer(modifier = Modifier.height(8.dp))

        activityTypes.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { name ->
                    Button(
                        onClick = {
                            currentTime = Clock.System.now()
                            viewModel.onNewActivity(name)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(name)
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(text = "Управление:")
        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Button(
            onClick = { viewModel.onDeleteLastActivity() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Удалить последнюю запись")
        }

        Button(
            onClick = {
                // TODO: Add a note via alert window
                // TODO: Make it available to add note to any note by id?
                viewModel.onAddNoteToLastActivityUseCase("Заметка")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить заметку")
        }

        Button(
            onClick = {
                viewModel.onSaveActivities()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить файл")
        }
    }
}
