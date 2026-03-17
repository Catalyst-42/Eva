package com.grace.eva.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.domain.model.Activities
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.utils.formatDuration
import com.grace.eva.utils.formatTime
import kotlinx.coroutines.delay
import kotlin.time.Clock.System.now
import kotlin.time.Duration
import kotlin.time.Instant

@Composable
fun ActivitiesCard(
    activities: Activities,
    viewModel: TrackerViewModel,
    onNameChange: (String) -> Unit,
    expanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(expanded) }
    var editedName by remember(activities.name) { mutableStateOf(activities.name) }
    var editedEnd by remember(activities.end) { mutableStateOf(activities.end) }
    var duration by remember { mutableStateOf(Duration.ZERO) }

    LaunchedEffect(activities.activities.size) {
        while (true) {
            duration = now() - (activities.activities.firstOrNull()?.begin ?: now())
            delay(1000)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activities.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                val begin = if (activities.activities.isNotEmpty()) {
                    formatTime(activities.activities.first().begin, "dd.mm.yyyy")
                } else {
                    "Не начато"
                }

                Text(
                    text = "От $begin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Этапов: ${activities.activities.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                ActivitiesCardControls(
                    activities = activities,
                    editedName = editedName,
                    editedEnd = editedEnd,
                    viewModel = viewModel,
                    onNameChange = {
                        editedName = it
                        onNameChange(it)
                    },
                    onEndChange = { /* TODO: Make available to set end time to now() */ },
                    onActivitiesSave = { viewModel.onSaveActivities() }
                )
            }
        }
    }
}

@Composable
fun ActivitiesCardControls(
    activities: Activities,
    editedName: String,
    editedEnd: Instant?,
    viewModel: TrackerViewModel,
    onNameChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onActivitiesSave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = editedName,
            onValueChange = onNameChange,
            label = { Text("Название сохранения") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = if (editedEnd != null) formatTime(editedEnd) else "Нет",
            onValueChange = onEndChange,
            label = { Text("Завершено") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = editedEnd != null,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { viewModel.onActivitiesExport(activities) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Экспортировать")
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { /* TODO: Complete save */ },
            modifier = Modifier.weight(1f)
        ) {
            Text("Завершить")
        }

        Button(
            onClick = { /* TODO: Activate save */ },
            modifier = Modifier.weight(1f),
        ) {
            Text("Активировать")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { /* TODO: Delete save */ },
            modifier = Modifier.weight(1f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline)
        ) {
            Text("Удалить")
        }

        Button(
            onClick = { onActivitiesSave() },
            modifier = Modifier.weight(1f)
        ) {
            Text("Сохранить")
        }
    }
}

@Preview
@Composable
fun PreviewActivitiesCard() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }

    ActivitiesCard(
        activities = mockViewModel.uiState.value.activities,
        viewModel = mockViewModel,
        onNameChange = {},
        expanded = true
    )
}
