package com.grace.eva.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.grace.eva.util.formatDuration
import com.grace.eva.util.formatFloat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@Composable
fun WeeklyValueRow(
    name: String,
    duration: Duration,
    density: Double,
    previousDensity: Double? = null,
    previousWeekNumber: Int? = null,
    activityCount: Int = 1,
    averagePerDay: Duration? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Время",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Плотность",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${formatFloat(density.toFloat(), 1)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Число этапов",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = activityCount.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        averagePerDay?.let { avgDuration ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "В среднем в день",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(avgDuration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        previousDensity?.let { previous ->
            val change = density - previous
            val prefix = if (change > 0.0) "+" else ""
            val changeColor = when {
                change > 0.0 -> Color(0xFF2E7D32)
                change < 0.0 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Динамика за $previousWeekNumber неделю",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$prefix${formatFloat(change.toFloat(), 1)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = changeColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeeklyValueRowPreview() {
    WeeklyValueRow(
        name = "Работа",
        duration = 8.hours,
        density = 35.5,
        previousDensity = 32.1,
        previousWeekNumber = 1,
        activityCount = 5,
        averagePerDay = 1.5.hours
    )
}

@Preview(showBackground = true)
@Composable
fun WeeklyValueRowPreviewWithoutPrevious() {
    WeeklyValueRow(
        name = "Учеба",
        duration = 4.hours,
        density = 20.0,
        activityCount = 3,
        averagePerDay = 0.8.hours
    )
}

@Preview(showBackground = true)
@Composable
fun WeeklyValueRowPreviewEmpty() {
    WeeklyValueRow(
        name = "Отдых",
        duration = Duration.ZERO,
        density = 0.0,
        activityCount = 0,
        averagePerDay = Duration.ZERO
    )
}