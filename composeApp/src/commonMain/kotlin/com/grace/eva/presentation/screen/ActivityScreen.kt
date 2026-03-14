package com.grace.eva.presentation.screen

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grace.eva.di.AppContainer
import com.grace.eva.domain.model.Activity
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.utils.formatDuration
import kotlinx.coroutines.delay
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock.System
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun ActivityScreen(
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

    Column {
        ActivitiesTotal(activities)
        ActivitiesList(
            activities,
            onActivityChange = { activity -> viewModel.onUpdateActivity(activity) },
            onActivityDelete = { activity -> viewModel.onDeleteActivity(activity) },
        )
    }
}

@Composable
fun ActivitiesTotal(activities: MutableList<Activity>) {
    var duration by remember { mutableStateOf(Duration.ZERO) }

    LaunchedEffect(activities.size) {
        while (true) {
            duration = now() - (
                activities.firstOrNull()?.begin ?: now()
            )
            delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Активностей: ${activities.size}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Всего ${formatDuration(duration)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = activities.reversed(),
            key = { activity -> "${activity.id}-${activity.end}" }
        ) { activity ->
            ActivityCard(
                activity = activity,
                onActivityChange = { activity -> onActivityChange(activity) },
                onActivityDelete = { activity -> onActivityDelete(activity) }
            )
        }
    }
}

@Preview
@Composable
fun PreviewActivitiesTotal() {
    val activities: MutableList<Activity> = mutableListOf(
        Activity("Первое", "", now() - 1.hours),
        Activity("Второе", "", now() - 2.hours, end = now() - 1.hours),
        Activity("Третье", "Начало начал", now() - 4.hours, end = now() - 3.hours),
    )

    ActivitiesTotal(activities)
}

@Preview
@Composable
fun PreviewActivitiesList() {
    val activities: MutableList<Activity> = mutableListOf(
        Activity("Первое", "", now() - 1.hours),
        Activity("Второе", "", now() - 2.hours, end = now() - 1.hours),
        Activity("Третье", "Начало начал", now() - 4.hours, end = now() - 3.hours),
    )

    ActivitiesList(activities, {}, {})
}
