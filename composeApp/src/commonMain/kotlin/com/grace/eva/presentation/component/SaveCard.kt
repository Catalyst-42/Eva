package com.grace.eva.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuDefaults
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
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@Composable
fun SaveCard(
    save: Save,
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    expanded: Boolean = false,
    onMessage: (String) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(expanded) }
    var editedName by remember(save.id) { mutableStateOf(save.name) }
    var editedEnd by remember(save.id) { mutableStateOf(save.end) }
    var duration by remember { mutableStateOf(Duration.ZERO) }
    val cardInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    val state by viewModel.uiState.collectAsState()
    val isCurrentSave = state.currentSave?.id == save.id
    val isSaveActive = save.end == null
    val firstActivityBegin = save.activities.firstOrNull()?.begin

    // Reset edited values when save changes
    LaunchedEffect(save.id, save.name, save.end) {
        editedName = save.name
        editedEnd = save.end
    }

    // Timer for active save
    LaunchedEffect(save.activities.size, save.end) {
        if (isSaveActive && firstActivityBegin != null) {
            while (true) {
                duration = Clock.System.now() - firstActivityBegin
                delay(1000)
            }
        } else if (!isSaveActive && firstActivityBegin != null && save.end != null) {
            duration = save.end - firstActivityBegin
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium,
        interactionSource = cardInteractionSource,
        border = if (isCurrentSave) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else null,
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
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
                    onSaveSuccess = { isExpanded = false },
                    isCurrentSave = isCurrentSave,
                    onMessage = onMessage,
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
            "От ${formatTime(it, "dd.MM.yyyy")}"
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
    onSaveSuccess: () -> Unit,
    isCurrentSave: Boolean,
    onMessage: (String) -> Unit,
    viewModel: TrackerViewModel
) {
    var endText by remember(editedEnd) { mutableStateOf(editedEnd?.let { formatTime(it) } ?: "") }
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
            value = endText.ifBlank { "Нет" },
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
        onMessage = onMessage,
        onFormatError = { formatError = true },
        onFormatSuccess = { formatError = false },
        onValidationError = { validationError = it },
        onValidationSuccess = { validationError = null },
        onSaveSuccess = onSaveSuccess,
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
    onMessage: (String) -> Unit,
    onFormatError: () -> Unit,
    onFormatSuccess: () -> Unit,
    onValidationError: (String?) -> Unit,
    onValidationSuccess: () -> Unit,
    onNameSaved: (String) -> Unit,
    onEndSaved: (Instant?) -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: TrackerViewModel
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isSaveActive = save.end == null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            IconButton(
                onClick = { menuExpanded = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Действия"
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier
            ) {
                // End save
                if (isSaveActive) {
                    DropdownMenuItem(
                        text = { Text("Завершить сохранение") },
                        onClick = {
                            viewModel.onCompleteSave(save)
                            menuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Продолжить сохранение") },
                        onClick = {
                            viewModel.onContinueSave(save)
                            menuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                        }
                    )
                }

                // Archive/Unarchive
                DropdownMenuItem(
                    text = {
                        Text(if (save.isArchived) "Вернуть из архива" else "Архивировать")
                    },
                    onClick = {
                        viewModel.onSetSaveArchived(save, !save.isArchived)
                        menuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (save.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = null
                        )
                    }
                )

                // Export
                DropdownMenuItem(
                    text = { Text("Экспортировать") },
                    onClick = {
                        viewModel.onExportSave(save)
                        menuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ImportExport,
                            contentDescription = null
                        )
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                // Sync
                DropdownMenuItem(
                    text = { Text("Синхронизировать") },
                    onClick = {
                        viewModel.onSyncSave(
                            save = save,
                            onError = { message ->
                                onMessage(message)
                            },
                            onSuccess = {
                                onMessage("Сохранение синхронизировано")
                            }
                        )
                        menuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null
                        )
                    }
                )

                // Delete
                DropdownMenuItem(
                    text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        viewModel.onDeleteSave(save)
                        menuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    colors = MenuDefaults.itemColors(
                        leadingIconColor = MaterialTheme.colorScheme.error
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Save and load
        if (isCurrentSave) {
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

                            onSaveSuccess()
                        }
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Сохранить")
            }
        } else {
            Button(
                onClick = { viewModel.onSetCurrentSave(save) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Загрузить")
            }
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
            appContainer = MockAppContainer(MockType.SIMPLE)
        )
    }

    SaveCard(
        save = mockViewModel.uiState.value.currentSave ?: Save(),
        viewModel = mockViewModel,
        expanded = true
    )
}