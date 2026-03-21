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
import androidx.compose.runtime.collectAsState
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
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.utils.formatDuration
import com.grace.eva.utils.formatTime
import com.grace.eva.utils.parseInstant
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun ActivityCard(
    activity: com.grace.eva.domain.model.Activity?,
    viewModel: TrackerViewModel,
    expanded: Boolean = false
) {
    if (activity == null) {
        EmptyActivityCard()
        return
    }

    var isExpanded by remember { mutableStateOf(expanded) }
    var editedName by remember(activity.id) { mutableStateOf(activity.name) }
    var editedNote by remember(activity.id) { mutableStateOf(activity.note) }

    val state by viewModel.uiState.collectAsState()
    val currentSave = state.currentSave
    val isSaveActive = currentSave?.end == null
    val isActivityActive = isSaveActive && activity == currentSave?.activities?.lastOrNull()

    LaunchedEffect(activity.id, activity.name, activity.note) {
        editedName = activity.name
        editedNote = activity.note
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            // containerColor = if (isActivityActive)
                MaterialTheme.colorScheme.surfaceContainer
            // else
            //     MaterialTheme.colorScheme.surfaceС
        ),
        border = if (isActivityActive) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatTime(activity.begin, "dd.mm HH:MM:SS"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = activity.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Isolated duration text that updates independently
                ActivityDurationText(
                    activity = activity,
                    isActivityActive = isActivityActive,
                    viewModel = viewModel,
                    currentSave = currentSave
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                ActivityCardControls(
                    activity = activity,
                    editedName = editedName,
                    editedNote = editedNote,
                    onNameChange = { editedName = it },
                    onNoteChange = { editedNote = it },
                    viewModel = viewModel,
                    onSaveSuccess = {
                        // Keep the card expanded after successful save
                        // isExpanded remains true, no change
                    }
                )
            }
        }
    }
}

@Composable
private fun ActivityDurationText(
    activity: com.grace.eva.domain.model.Activity,
    isActivityActive: Boolean,
    viewModel: TrackerViewModel,
    currentSave: com.grace.eva.domain.model.Save?
) {
    var currentTime by remember { mutableStateOf(Clock.System.now()) }

    // Update current time every second only when activity is active
    LaunchedEffect(isActivityActive) {
        if (isActivityActive) {
            while (true) {
                delay(1000L)
                currentTime = Clock.System.now()
            }
        }
    }

    // Get activity end time based on current state
    val activityEndTime = if (isActivityActive) {
        currentTime
    } else {
        viewModel.getActivityEndTime(activity) ?: currentSave?.end ?: currentTime
    }

    val duration = remember(activity.begin, activityEndTime) {
        formatDuration(activityEndTime - activity.begin)
    }

    Text(
        text = duration,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun EmptyActivityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "Нет активной активности",
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActivityCardControls(
    activity: com.grace.eva.domain.model.Activity,
    editedName: String,
    editedNote: String,
    onNameChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    viewModel: TrackerViewModel,
    onSaveSuccess: () -> Unit
) {
    var beginText by remember(activity.begin) { mutableStateOf(formatTime(activity.begin)) }
    var formatError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = beginText,
            onValueChange = { beginText = it },
            label = { Text("Начало") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = formatError || validationError != null,
            supportingText = if (formatError || validationError != null) {
                {
                    when {
                        formatError -> Text("Неверный формат: дд.мм.гггг ЧЧ:ММ:СС")
                        else -> Text(validationError!!)
                    }
                }
            } else null
        )

        OutlinedTextField(
            value = editedName,
            onValueChange = onNameChange,
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = editedNote,
            onValueChange = onNoteChange,
            label = { Text("Заметка") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }

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
                val newBegin = parseInstant(beginText)
                if (newBegin == null) {
                    formatError = true
                    return@Button
                }

                formatError = false
                validationError = null

                viewModel.onUpdateActivity(
                    activity = activity,
                    newName = editedName,
                    newNote = editedNote,
                    newBegin = newBegin,
                    onError = { errorMessage ->
                        validationError = errorMessage
                    },
                    onSuccess = {
                        validationError = null
                        onSaveSuccess()
                    }
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