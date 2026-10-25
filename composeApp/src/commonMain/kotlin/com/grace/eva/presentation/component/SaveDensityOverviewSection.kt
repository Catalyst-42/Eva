package com.grace.eva.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.grace.eva.presentation.component.chart.WeeklyDensityChart
import com.grace.eva.presentation.statistics.ActivityStat
import com.grace.eva.presentation.statistics.WeeklyStatistics
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.util.formatDuration
import com.grace.eva.util.formatFloat
import kotlin.time.Duration.Companion.seconds

@Composable
fun SaveDensityOverviewSection(
    weeks: List<WeeklyStatistics>,
    activityName: String?,
    activityStats: List<ActivityStat>,
    chartHeight: Int,
    onRefresh: () -> Unit,
    getColorForActivity: (String, Int) -> Color,
    viewModel: TrackerViewModel
) {
    val displayedActivityStats = activityStats.filter {
        activityName == null || it.name == activityName
    }
    val displayedActivityCount = displayedActivityStats.sumOf { it.count }
    val displayedTotalDuration = displayedActivityStats
        .sumOf { it.totalDuration.inWholeSeconds }.seconds

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Карта сохранения",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Обновить статистику")
            }
        }
        
        val backgroundLabel = when {
            activityName != null -> {
                val selectedDuration = weeks.sumOf { week ->
                    week.activityDurations
                        .firstOrNull { it.activityName == activityName }
                        ?.duration?.inWholeSeconds ?: 0L
                }
                val elapsedDuration = weeks.sumOf { it.totalDuration.inWholeSeconds }
                if (elapsedDuration > 0) {
                    formatFloat(
                        selectedDuration.toDouble().div(elapsedDuration).times(100.0).toFloat(),
                        1
                    ) + "%"
                } else {
                    null
                }
            }
            else -> {
                val visibleDuration = weeks.sumOf { week ->
                    week.activityDurations
                        .filter { !viewModel.getActivityTemplateIsHidden(it.activityName) }
                        .sumOf { it.duration.inWholeSeconds }
                }
                val elapsedDuration = weeks.sumOf { it.totalDuration.inWholeSeconds }
                if (elapsedDuration > 0) {
                    val percentage = formatFloat(
                        visibleDuration.toDouble().div(elapsedDuration).times(100.0).toFloat(),
                        1
                    ) + "%"
                    
                    val hasHiddenActivities = activityStats.any { viewModel.getActivityTemplateIsHidden(it.name) }
                    if (hasHiddenActivities && visibleDuration.toDouble().div(elapsedDuration) > 0.4) {
                        "$percentage (верх)"
                    } else {
                        percentage
                    }
                } else {
                    null
                }
            }
        }
        
        WeeklyDensityChart(
            weeks = weeks,
            getColorForActivity = getColorForActivity,
            activityName = activityName,
            backgroundLabel = backgroundLabel,
            showScale = false,
            showWeekNumbers = false,
            chartHeight = chartHeight.dp,
            isActivityHidden = { activityName ->
                viewModel.getActivityTemplateIsHidden(activityName)
            },
            backgroundLabelPosition = if (activityName == null && backgroundLabel?.contains("(верх)") == true) 0.75f else 0.5f
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Всего этапов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayedActivityCount.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Общее время",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(displayedTotalDuration),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}