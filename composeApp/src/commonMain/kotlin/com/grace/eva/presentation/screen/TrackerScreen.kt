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
import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import com.grace.eva.presentation.component.ActivitiesCard
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

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
    val activityTypes = listOf("Сон", "Отдых", "Пары", "Транспорт", "Домашка", "Другое")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ActivityCard(
            activity = state.activities.activities.lastOrNull(),
            onActivityChange = { updatedActivity -> viewModel.onUpdateActivity(updatedActivity) },
            onActivityDelete = { thisActivity -> viewModel.onDeleteActivity(thisActivity) }
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
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackerScreen() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer())
    }

    TrackerScreenContent(mockViewModel)
}
