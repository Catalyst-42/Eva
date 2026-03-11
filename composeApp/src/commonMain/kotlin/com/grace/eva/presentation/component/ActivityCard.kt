package com.grace.eva.presentation.component

import com.grace.eva.domain.model.Activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grace.eva.utils.formatDuration
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
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
                text = "Выберите этап для начала работы",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun PreviewActivityEmptyCard() {
    ActivityEmptyCard()
}

@Composable
fun ActivityCard(
    activity: Activity?,
    now: Instant
) {
    if (activity == null) {
        return ActivityEmptyCard()
    }

    // Display activity
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (activity.end == null)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Basic info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name and start time
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = activity.begin.toString(), // TODO: Make util to format time as dd.mm hh:mm:ss
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Duration
                val endTime = activity.end ?: now
                val duration = endTime - activity.begin

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
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )

                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (activity.end == null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewActivityCard() {
    val name = "Сон"
    val begin = Clock.System.now() - 42.seconds

    val activity = Activity(name, "", begin)
    ActivityCard(activity, Clock.System.now())
}

@Preview
@Composable
fun PreviewActivityInfoCard() {
    val name = "Другое"
    val note =  "Взбирался на Ковалькор, в отчие годы известный под именем Аграты"
    val begin = Clock.System.now() - 16.seconds

    val activity = Activity(name, note, begin)
    ActivityCard(activity, Clock.System.now())
}

@Preview
@Composable
fun PreviewActivityDoneCard() {
    val name = "Учёба"
    val note =  "Взбирался на Ковалькор, в отчие годы известный под именем Аграты"
    val begin = Clock.System.now() - 1.hours - 42.minutes - 16.seconds
    val end = Clock.System.now()

    val activity = Activity(name, note, begin, end)
    ActivityCard(activity, Clock.System.now())
}
