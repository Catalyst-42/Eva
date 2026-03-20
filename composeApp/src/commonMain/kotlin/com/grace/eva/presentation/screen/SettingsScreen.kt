package com.grace.eva.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.grace.eva.presentation.component.SaveCard
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
    val allSaves = state.allSaves
    val currentSave = state.currentSave

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Настройки сохранений",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (allSaves.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Text(
                    text = "Нет сохранений. Создайте новое.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            allSaves.forEach { save ->
                SaveCard(
                    save = save,
                    viewModel = viewModel,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Button(
            onClick = {
                viewModel.onCreateSave("Новое сохранение ${allSaves.size + 1}")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать новое сохранение")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.onImportSave()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Импортировать файл сохранения")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.LARGE))
    }

    SettingsScreenContent(
        viewModel = mockViewModel
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen_NoSaves() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.EMPTY))
    }

    SettingsScreenContent(
        viewModel = mockViewModel
    )
}