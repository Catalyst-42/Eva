package com.grace.eva.presentation.component

import androidx.compose.foundation.clickable
import com.grace.eva.domain.model.Activity
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.utils.formatDuration
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import com.grace.eva.utils.formatTime
import kotlin.time.Instant

@Composable
fun ActivityEmptyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Тут будет текущая активность",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActivityCard(
    activity: Activity?,
    viewModel: TrackerViewModel,
    expanded: Boolean = false
) {
    if (activity == null) {
        return ActivityEmptyCard()
    }

    var expanded by remember { mutableStateOf(expanded) }
    var editedName by remember(activity.name) { mutableStateOf(activity.name) }
    var editedNote by remember(activity.note) { mutableStateOf(activity.note) }

    var currentTime by remember { mutableStateOf(Clock.System.now()) }

    val state by viewModel.uiState.collectAsState()
    val isSaveCompleted = viewModel.isCurrentSaveCompleted()
    val endTime = viewModel.getActivityEndTime(activity) ?: currentTime
    val isActive = !isSaveCompleted && viewModel.getActivityEndTime(activity) == null

    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                currentTime = Clock.System.now()
                delay(1000L)
            }
        }
    }

    val duration = remember(activity.begin, endTime, currentTime) {
        formatDuration(endTime - activity.begin)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activity.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = formatTime(activity.begin, "dd.mm HH:MM:SS"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activity.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        )

                        Text(
                            text = duration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (expanded) {
                ActivityCardControls(
                    editedName = editedName,
                    editedNote = editedNote,
                    editedBegin = activity.begin,
                    activity = activity,
                    viewModel = viewModel,
                    onNameChange = { editedName = it },
                    onNoteChange = { editedNote = it }
                )
            }
        }
    }
}

@Composable
fun ActivityCardControls(
    editedName: String,
    editedNote: String,
    editedBegin: Instant,
    activity: Activity,
    viewModel: TrackerViewModel,
    onNameChange: (String) -> Unit,
    onNoteChange: (String) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = formatTime(editedBegin),
        onValueChange = { /* TODO: Make editable */ },
        label = { Text("Начало") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = false
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = editedName,
        onValueChange = onNameChange,
        label = { Text("Название") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = editedNote,
        onValueChange = onNoteChange,
        label = { Text("Заметка") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = { viewModel.onRemoveActivity(activity) },
            modifier = Modifier.weight(1f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        ) {
            Text("Удалить")
        }

        Button(
            onClick = {
                viewModel.onUpdateActivity(
                    activity.copy(
                        name = editedName,
                        note = editedNote
                    )
                )
            },
            modifier = Modifier.weight(1f)
        ) {
            Text("Сохранить")
        }
    }
}

@Preview
@Composable
fun PreviewActivityCard() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }
    val activity = mockViewModel.uiState.value.currentSave?.activities?.firstOrNull()
    ActivityCard(activity, mockViewModel)
}

@Preview
@Composable
fun PreviewActivityCardExpanded() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }
    val activity = mockViewModel.uiState.value.currentSave?.activities?.firstOrNull()
    ActivityCard(activity, mockViewModel, expanded = true)
}

@Preview
@Composable
fun PreviewListActivityCardExpanded() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.LARGE))
    }
    val activities = mockViewModel.uiState.value.currentSave?.activities ?: emptyList()
    ActivityCard(activities.firstOrNull(), mockViewModel, expanded = true)
}

@Preview
@Composable
fun PreviewActivityCard_Completed() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }
    val activity = mockViewModel.uiState.value.currentSave?.activities?.firstOrNull()
    ActivityCard(activity, mockViewModel)
}