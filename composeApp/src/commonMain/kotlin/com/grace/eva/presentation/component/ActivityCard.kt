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
import com.grace.eva.utils.formatDuration
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedTextField
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
    nextActivityBegin: Instant?,
    onActivityChange: (Activity) -> Unit,
    onActivityDelete: (Activity) -> Unit,
    expanded: Boolean = false
) {
    if (activity == null) {
        return ActivityEmptyCard()
    }

    var expanded by remember { mutableStateOf(expanded) }
    var editedName by remember(activity.name) { mutableStateOf(activity.name) }
    var editedNote by remember(activity.note) { mutableStateOf(activity.note) }
    var editedBegin by remember(activity.begin) { mutableStateOf(activity.begin) }

    var currentTime by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(nextActivityBegin) {
        if (nextActivityBegin == null) {
            while (true) {
                currentTime = Clock.System.now()
                delay(1000L)
            }
        }
    }

    val duration = remember(activity.begin, nextActivityBegin, currentTime) {
        val endTime = nextActivityBegin ?: currentTime
        formatDuration(endTime - activity.begin)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (nextActivityBegin == null)
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
                    editedBegin = editedBegin,
                    onNameChange = { editedName = it },
                    onNoteChange = { editedNote = it },
                    onSave = {
                        onActivityChange(
                            activity.copy(
                                name = editedName,
                                note = editedNote
                            )
                        )
                        expanded = false
                    },
                    onDelete = {
                        onActivityDelete(activity)
                        expanded = false
                    }
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
    onNameChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = formatTime(editedBegin),
        onValueChange = onNoteChange,
        label = { Text("Начало") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = false // TODO: Make editable
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
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        ) {
            Text("Удалить")
        }

        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f)
        ) {
            Text("Сохранить")
        }
    }
}

@Preview
@Composable
fun PreviewActivityCard() {
    val name = "Сон"
    val begin = Clock.System.now() - 666.seconds
    val activity = Activity(name, "", begin)
    ActivityCard(activity, null, {}, {})
}

@Preview
@Composable
fun PreviewActivityCardExpanded() {
    val name = "И ещё что-то"
    val note = "Здоровый сон"
    val begin = Clock.System.now() - 42.seconds
    val activity = Activity(name, note, begin)
    ActivityCard(activity, null, {}, {}, true)
}

@Preview
@Composable
fun PreviewListActivityCardExpanded() {
    val name = "И ещё что-то"
    val note = "Нездоровый сон"
    val begin = Clock.System.now() - 3.hours - 42.minutes - 16.seconds
    val activity = Activity(name, note, begin)
    val nextBegin = Clock.System.now() - 2.hours
    ActivityCard(activity, nextBegin, {}, {}, true)
}