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
    var isExpanded by remember { mutableStateOf(expanded) }
    var editedName by remember(save.name) { mutableStateOf(save.name) }
    var editedEnd by remember(save.end) { mutableStateOf(save.end) }
    var duration by remember { mutableStateOf(Duration.ZERO) }

    val state by viewModel.uiState.collectAsState()
    val isCurrentSave = state.currentSave?.id == save.id
    val isSaveActive = save.end == null
    val firstActivityBegin = save.activities.firstOrNull()?.begin

    // Timer for active save
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium,
        border = if (isCurrentSave) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else null,
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            SaveCardHeader(
                name = save.name,
                beginDate = save.activities.firstOrNull()?.begin,
                activitiesCount = save.activities.size,
                duration = duration
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                SaveCardForm(
                    save = save,
                    editedName = editedName,
                    editedEnd = editedEnd,
                    onNameChange = { editedName = it },
                    onEndChange = { editedEnd = it },
                    isCurrentSave = isCurrentSave,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun SaveCardHeader(
    name: String,
    beginDate: Instant?,
    activitiesCount: Int,
    duration: Duration
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        val beginText = beginDate?.let {
            "От ${formatTime(it, "dd.mm.yyyy")}"
        } ?: "Не начато"

        Text(
            text = beginText,
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
            text = "Этапов: $activitiesCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SaveCardForm(
    save: Save,
    editedName: String,
    editedEnd: Instant?,
    onNameChange: (String) -> Unit,
    onEndChange: (Instant?) -> Unit,
    isCurrentSave: Boolean,
    viewModel: TrackerViewModel
) {
    var endText by remember(editedEnd) { mutableStateOf(editedEnd?.let { formatTime(it) } ?: "Нет") }
    var formatError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

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

        OutlinedTextField(
            value = endText,
            onValueChange = { endText = it },
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

    Spacer(modifier = Modifier.height(16.dp))

    SaveCardActions(
        save = save,
        editedName = editedName,
        endText = endText,
        isCurrentSave = isCurrentSave,
        onFormatError = { formatError = true },
        onFormatSuccess = { formatError = false },
        onValidationError = { validationError = it },
        onValidationSuccess = { validationError = null },
        onNameSaved = onNameChange,
        onEndSaved = onEndChange,
        viewModel = viewModel
    )
}

@Composable
private fun SaveCardActions(
    save: Save,
    editedName: String,
    endText: String,
    isCurrentSave: Boolean,
    onFormatError: () -> Unit,
    onFormatSuccess: () -> Unit,
    onValidationError: (String?) -> Unit,
    onValidationSuccess: () -> Unit,
    onNameSaved: (String) -> Unit,
    onEndSaved: (Instant?) -> Unit,
    viewModel: TrackerViewModel
) {
    Button(
        onClick = { viewModel.onExportSave(save) },
        modifier = Modifier.fillMaxWidth()
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
            modifier = Modifier.weight(1f)
        ) {
            Text(if (save.end == null) "Завершить" else "Продолжить")
        }

        Button(
            onClick = { viewModel.onSetCurrentSave(save) },
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
            onClick = { viewModel.onDeleteSave(save) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Удалить")
        }

        Button(
            onClick = {
                // Parse time and validate
                var newEnd: Instant? = null

                if (endText.isNotBlank()) {
                    val parsed = parseInstant(endText)
                    if (parsed == null) {
                        onFormatError()
                        return@Button
                    }
                    onFormatSuccess()
                    newEnd = parsed
                }

                // Update save
                viewModel.onUpdateSave(
                    save = save,
                    newName = editedName,
                    newEnd = newEnd,
                    onError = { errorMessage ->
                        onValidationError(errorMessage)
                    },
                    onSuccess = {
                        onValidationSuccess()
                        if (editedName != save.name) {
                            onNameSaved(editedName)
                        }
                        if (newEnd != save.end) {
                            onEndSaved(newEnd)
                        }
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
fun PreviewSaveCard() {
    val mockViewModel = remember {
        TrackerViewModel(
            appContainer = MockAppContainer(MockType.SIMPLE)
        )
    }

    SaveCard(
        save = mockViewModel.uiState.value.currentSave ?: Save(),
        viewModel = mockViewModel,
    )
}

@Preview
@Composable
fun PreviewSaveCardExpanded() {
    val mockViewModel = remember {
        TrackerViewModel(
            appContainer = MockAppContainer(MockType.LARGE)
        )
    }

    SaveCard(
        save = mockViewModel.uiState.value.currentSave ?: Save(),
        viewModel = mockViewModel,
        expanded = true
    )
}