package com.grace.eva.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.grace.eva.domain.model.Activity
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel

@Composable
fun ActivitiesScreen(
    appContainer: AppContainer
) {
    val viewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModel.Factory(appContainer)
    )

    ActivityScreenContent(viewModel)
}

@Composable
fun ActivityScreenContent(viewModel: TrackerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val activities = state.activities.activities

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Последние активности",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ActivitiesList(
            activities,
            onActivityChange = { activity -> viewModel.onUpdateActivity(activity) },
            onActivityDelete = { activity -> viewModel.onDeleteActivity(activity) },
        )
    }
}

@Composable
fun ActivitiesList(
    activities: MutableList<Activity>,
    onActivityChange: (Activity) -> Unit,
    onActivityDelete: (Activity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = activities.reversed(),
            key = { activity -> "${activity.id}-${activity.end}" }) { activity ->
            ActivityCard(
                activity = activity,
                onActivityChange = { activity -> onActivityChange(activity) },
                onActivityDelete = { activity -> onActivityDelete(activity) })
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewActivitiesScreen() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.LARGE))
    }

    ActivityScreenContent(mockViewModel)
}
