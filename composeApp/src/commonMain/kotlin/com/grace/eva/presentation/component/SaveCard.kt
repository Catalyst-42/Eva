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
import com.grace.eva.domain.model.Save
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.util.formatDuration
import com.grace.eva.util.formatTime
import com.grace.eva.util.parseInstant
import kotlinx.coroutines.delay
import kotlin.time.Clock.System.now
import kotlin.time.Duration
import kotlin.time.Instant

@Composable
fun SaveCard(
    save: Save,
    viewModel: TrackerViewModel,
    expanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(expanded) }
    var editedName by remember(save.name) { mutableStateOf(save.name) }
    var editedEnd by remember(save.end) { mutableStateOf(save.end) }
    var duration by remember { mutableStateOf(Duration.ZERO) }

    val state by viewModel.uiState.collectAsState()
    val isCurrentSave = state.currentSave?.id == save.id
    val isSaveActive = save.end == null

    val firstActivityBegin = save.activities.firstOrNull()?.begin

    LaunchedEffect(save.activities.size, save.end) {
        if (isSaveActive && firstActivityBegin != null) {
            while (true) {
                duration = now() - firstActivityBegin
                delay(1000)
            }
        } else if (!isSaveActive && firstActivityBegin != null) {
            duration = save.end - firstActivityBegin
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (isCurrentSave) {
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
                    text = save.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                val begin = if (save.activities.isNotEmpty()) {
                    formatTime(save.activities.first().begin, "dd.mm.yyyy")
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Этапов: ${save.activities.size}",
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

                SaveCardControls(
                    save = save,
                    editedName = editedName,
                    editedEnd = editedEnd,
                    onNameChange = { editedName = it },
                    onEndChange = { newEnd ->
                        editedEnd = newEnd
                    },
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
fun SaveCardControls(
    save: Save,
    editedName: String,
    editedEnd: Instant?,
    onNameChange: (String) -> Unit,
    onEndChange: (Instant?) -> Unit,
    viewModel: TrackerViewModel,
) {
    var localEditedEnd by remember(editedEnd) { mutableStateOf(editedEnd) }
    var endText by remember(editedEnd) { mutableStateOf(editedEnd?.let { formatTime(it) } ?: "") }
    var formatError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(editedEnd) {
        localEditedEnd = editedEnd
        endText = editedEnd?.let { formatTime(it) } ?: ""
    }

    val state by viewModel.uiState.collectAsState()
    val isCurrentSave = state.currentSave?.id == save.id

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = editedName,
            onValueChange = onNameChange,
            label = { Text("Название сохранения") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Column {
            OutlinedTextField(
                value = endText,
                onValueChange = {
                    endText = it
                },
                label = { Text("Завершено") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = formatError || validationError != null,
                enabled = save.end != null,
                supportingText = if (formatError || validationError != null) {
                    {
                        when {
                            formatError -> Text("Неверный формат: дд.мм.гггг ЧЧ:ММ:СС")
                            else -> Text(validationError!!)
                        }
                    }
                } else null
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { viewModel.onExportSave(save) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Экспортировать")
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                if (save.end == null) {
                    viewModel.onCompleteSave(save)
                } else {
                    viewModel.onContinueSave(save)
                }
            },
            modifier = Modifier.weight(1f),
        ) {
            Text(if (save.end == null) "Завершить" else "Продолжить")
        }

        Button(
            onClick = {
                viewModel.onSetCurrentSave(save)
            },
            modifier = Modifier.weight(1f),
            enabled = !isCurrentSave
        ) {
            Text(if (isCurrentSave) "Загружено" else "Загрузить")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = {
                viewModel.onDeleteSave(save)
            },
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
                // Update name if changed
                if (editedName != save.name) {
                    viewModel.onRenameSave(save, editedName)
                }

                // Parse and validate end time
                if (endText.isNotBlank()) {
                    val newEnd = parseInstant(endText)
                    if (newEnd == null) {
                        formatError = true
                        return@Button
                    } else {
                        formatError = false
                        validationError = null
                    }

                    if (newEnd != save.end) {
                        viewModel.onUpdateSaveEndWithCallback(
                            save = save,
                            newEnd = newEnd,
                            onError = { errorMessage ->
                                validationError = errorMessage
                            },
                            onSuccess = {
                                validationError = null
                                onEndChange(newEnd)
                            }
                        )
                    }
                } else {
                    // Empty field means no end time (active save)
                    if (save.end != null) {
                        viewModel.onUpdateSaveEndWithCallback(
                            save = save,
                            newEnd = null,
                            onError = { errorMessage ->
                                validationError = errorMessage
                            },
                            onSuccess = {
                                validationError = null
                                onEndChange(null)
                            }
                        )
                    }
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Text("Сохранить")
        }
    }
}

@Preview
@Composable
fun PreviewSaveCard() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }

    SaveCard(
        save = mockViewModel.uiState.value.currentSave ?: Save(),
        viewModel = mockViewModel,
        expanded = true
    )
}

@Preview
@Composable
fun PreviewSaveCardCompleted() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }

    val completedSave = (mockViewModel.uiState.value.currentSave ?: Save()).copy(
        end = now()
    )

    SaveCard(
        save = completedSave,
        viewModel = mockViewModel,
        expanded = true
    )
}