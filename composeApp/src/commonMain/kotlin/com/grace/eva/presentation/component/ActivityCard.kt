package com.grace.eva.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.util.formatDuration
import com.grace.eva.util.formatTime
import com.grace.eva.util.parseInstant
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

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
    var isTemplateNameDropdownExpanded by remember { mutableStateOf(false) }

    val state by viewModel.uiState.collectAsState()
    val currentSave = state.currentSave
    val isSaveActive = currentSave?.end == null
    val isActivityActive = isSaveActive && activity == currentSave?.activities?.lastOrNull()

    LaunchedEffect(activity.id, activity.name, activity.note) {
        editedName = activity.name
        editedNote = activity.note
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (isActivityActive) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        } else {
            null
        },
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
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
                verticalAlignment = Alignment.CenterVertically,
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

                Spacer(modifier = Modifier.width(8.dp))

                ActivityIcon(
                    viewModel.getTemplateForActivity(activity.name)
                        ?: ActivityTemplate(),
                    6.dp
                )

                Spacer(modifier = Modifier.width(8.dp))

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
                        isExpanded = false
                    },
                    activityTemplates = state.activityTemplates,
                    isDropdownExpanded = isTemplateNameDropdownExpanded,
                    onDropdownExpandChange = { isTemplateNameDropdownExpanded = it }
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
                delay(1000L.milliseconds)
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
            text = "Нет последней активности",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActivityCardHeaderDropdownMenu(
    onInsertEmptyStageBefore: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
            // Insert empty stage before
            DropdownMenuItem(
                text = { Text("Добавить активность ранее") },
                onClick = {
                    onInsertEmptyStageBefore()
                    menuExpanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ViewAgenda,
                        contentDescription = null
                    )
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // Delete
            DropdownMenuItem(
                text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    onDelete()
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
}

@Composable
private fun ActivityNameComboBox(
    value: String,
    onValueChange: (String) -> Unit,
    activityTemplates: List<ActivityTemplate>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdownButton by remember { mutableStateOf(false) }
    var textFieldWidthPx by remember { mutableStateOf(0) }
    var textFieldHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    LaunchedEffect(activityTemplates.size) {
        showDropdownButton = activityTemplates.isNotEmpty()
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldWidthPx = coordinates.size.width
                },
            label = { Text("Название") },
            singleLine = true,
            trailingIcon = {
                if (showDropdownButton) {
                    IconButton(
                        onClick = { onExpandChange(!isExpanded) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Скрыть список" else "Показать список"
                        )
                    }
                }
            }
        )

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandChange(false) },
            offset = DpOffset(
                x = 0.dp,
                y = with(density) { textFieldHeightPx.toDp() }
            ),
            modifier = Modifier
                .width(with(density) { textFieldWidthPx.toDp() })
                .heightIn(max = 200.dp)
        ) {
            activityTemplates.forEach { template ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            ActivityIcon(template, 16.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = template.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    onClick = {
                        onValueChange(template.name)
                        onExpandChange(false)
                    }
                )
            }
        }
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
    onSaveSuccess: () -> Unit,
    activityTemplates: List<ActivityTemplate> = emptyList(),
    isDropdownExpanded: Boolean = false,
    onDropdownExpandChange: (Boolean) -> Unit = {}
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

        // Templates
        ActivityNameComboBox(
            value = editedName,
            onValueChange = onNameChange,
            activityTemplates = activityTemplates,
            isExpanded = isDropdownExpanded,
            onExpandChange = onDropdownExpandChange,
            modifier = Modifier.fillMaxWidth()
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        ActivityCardHeaderDropdownMenu(
            onInsertEmptyStageBefore = {
                viewModel.onInsertEmptyStageBefore(activity)
            },
            onDelete = {
                viewModel.onRemoveActivity(activity)
            }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
}

@Preview
@Composable
fun PreviewEmptyActivityCard() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }
    ActivityCard(null, mockViewModel)
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