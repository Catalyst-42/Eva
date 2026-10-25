package com.grace.eva.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grace.eva.presentation.component.chart.RepeatableIconButton
import com.grace.eva.presentation.statistics.ActivityStat
import com.grace.eva.presentation.statistics.WeeklyActivityDuration
import com.grace.eva.presentation.statistics.WeeklyStatistics
import com.grace.eva.util.formatDuration
import com.grace.eva.util.formatFloat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun StatisticsCard(
    stat: ActivityStat?,
    totalDays: Double,
    weeks: List<WeeklyStatistics>,
    selectedWeek: WeeklyStatistics?,
    selectedWeekIndex: Int,
    onWeekSelected: (Int) -> Unit
) {
    val navigationWeekIndex = selectedWeekIndex.takeIf { it in weeks.indices }
        ?: weeks.lastIndex
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedWeek != null) "Неделя ${selectedWeekIndex + 1}" else "Статистика",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                RepeatableIconButton(
                    onStep = { if (navigationWeekIndex > 0) onWeekSelected(navigationWeekIndex - 1) },
                    enabled = navigationWeekIndex > 0,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Предыдущая неделя",
                    tint = if (navigationWeekIndex > 0) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = if (weeks.isEmpty()) "0 из 0" else "${navigationWeekIndex + 1} из ${weeks.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RepeatableIconButton(
                    onStep = {
                        if (navigationWeekIndex < weeks.lastIndex) onWeekSelected(
                            navigationWeekIndex + 1
                        )
                    },
                    enabled = navigationWeekIndex in 0 until weeks.lastIndex,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Следующая неделя",
                    tint = if (navigationWeekIndex < weeks.lastIndex) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
                IconButton(
                    onClick = { if (weeks.isNotEmpty()) onWeekSelected(weeks.lastIndex) },
                    enabled = weeks.isNotEmpty(),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Перейти к последней неделе",
                        modifier = Modifier.size(20.dp),
                        tint = if (weeks.isNotEmpty()) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (selectedWeek != null) {
            val previousWeek = weeks.getOrNull(selectedWeekIndex - 1)
            val weeklyActivities = if (stat == null) {
                selectedWeek.activityDurations
            } else {
                listOf(
                    selectedWeek.activityDurations.firstOrNull { it.activityName == stat.name }
                        ?: WeeklyActivityDuration(stat.name, Duration.ZERO)
                )
            }
            weeklyActivities.forEach { activity ->
                val density = if (selectedWeek.totalDuration > Duration.ZERO) {
                    activity.duration.inWholeSeconds.toDouble() /
                            selectedWeek.totalDuration.inWholeSeconds.toDouble() * 100.0
                } else {
                    0.0
                }
                val previousDensity = previousWeek?.let { previous ->
                    val previousDuration = previous.activityDurations
                        .firstOrNull { it.activityName == activity.activityName }
                        ?.duration ?: Duration.ZERO
                    if (previous.totalDuration > Duration.ZERO) {
                        previousDuration.inWholeSeconds.toDouble() /
                                previous.totalDuration.inWholeSeconds.toDouble() * 100.0
                    } else {
                        0.0
                    }
                }

                val averagePerDay = if (activity.duration > Duration.ZERO) {
                    activity.duration.inWholeSeconds.seconds / 7
                } else {
                    null
                }

                WeeklyValueRow(
                    name = activity.activityName,
                    duration = activity.duration,
                    density = density,
                    previousDensity = previousDensity,
                    previousWeekNumber = if (previousDensity != null) selectedWeekIndex else null,
                    activityCount = 1,
                    averagePerDay = averagePerDay,
                )
            }
        }

        if (stat != null) {
            Text(
                text = "За всё время",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            val maxDensity = weeks.maxOfOrNull { week ->
                if (week.totalDuration > Duration.ZERO) {
                    val activityDuration =
                        week.activityDurations.firstOrNull { it.activityName == stat.name }
                    if (activityDuration != null && activityDuration.duration > Duration.ZERO) {
                        activityDuration.duration.inWholeSeconds.toDouble() / week.totalDuration.inWholeSeconds.toDouble() * 100.0
                    } else {
                        0.0
                    }
                } else {
                    0.0
                }
            } ?: 0.0

            val minDensity = weeks.filter { week -> week.totalDuration > Duration.ZERO }
                .minOfOrNull { week ->
                    val activityDuration =
                        week.activityDurations.firstOrNull { it.activityName == stat.name }
                    if (activityDuration != null && activityDuration.duration > Duration.ZERO) {
                        activityDuration.duration.inWholeSeconds.toDouble() / week.totalDuration.inWholeSeconds.toDouble() * 100.0
                    } else {
                        Double.MAX_VALUE
                    }
                }?.takeIf { it != Double.MAX_VALUE } ?: 0.0

            val averageDurationPerOccurrence = if (stat.count > 0) {
                stat.totalDuration / stat.count
            } else {
                Duration.ZERO
            }
            val averagePerDay = if (totalDays > 0) stat.totalDuration / totalDays else Duration.ZERO
            StatisticsValueRow("В среднем в неделю", formatDuration(averagePerDay * 7))
            StatisticsValueRow("В среднем в день", formatDuration(averagePerDay))
            StatisticsValueRow("В среднем за этап", formatDuration(averageDurationPerOccurrence))
            StatisticsValueRow("Максимальная длина этапа", formatDuration(stat.maxDuration))
            StatisticsValueRow("Минимальная длина этапа", formatDuration(stat.minDuration))
            StatisticsValueRow(
                "Максимум за неделю",
                "${formatFloat(maxDensity.toFloat(), 1)}%"
            )
            StatisticsValueRow(
                "Ненулевой минимум в неделю",
                "${formatFloat(minDensity.toFloat(), 1)}%"
            )
        }
    }
}