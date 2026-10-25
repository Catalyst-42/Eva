package com.grace.eva.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.grace.eva.presentation.component.chart.WeeklyDensityChart
import com.grace.eva.presentation.statistics.WeeklyStatistics
import com.grace.eva.presentation.viewmodel.TrackerViewModel

@Composable
fun WeeklyOverviewSection(
    weeks: List<WeeklyStatistics>,
    selectedWeekIndex: Int,
    activityName: String?,
    getColorForActivity: (String, Int) -> Color,
    viewModel: TrackerViewModel,
) {
    val weeksPerPage = 8
    val totalPages = (weeks.size + weeksPerPage - 1) / weeksPerPage
    var currentPage by remember { mutableStateOf(totalPages - 1) }

    fun goToWeekPage(weekIndex: Int) {
        val targetPage = weekIndex / weeksPerPage
        if (targetPage != currentPage && targetPage < totalPages) {
            currentPage = targetPage
        }
    }

    LaunchedEffect(selectedWeekIndex) {
        if (selectedWeekIndex >= 0 && selectedWeekIndex < weeks.size) {
            goToWeekPage(selectedWeekIndex)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Плотность",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (totalPages > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentPage > 0) currentPage--
                        },
                        enabled = currentPage > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Предыдущая страница",
                            modifier = Modifier.size(20.dp),
                            tint = if (currentPage > 0) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        text = "${currentPage + 1} из $totalPages",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = {
                            if (currentPage < totalPages - 1) currentPage++
                        },
                        enabled = currentPage < totalPages - 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Следующая страница",
                            modifier = Modifier.size(20.dp),
                            tint = if (currentPage < totalPages - 1) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        val startIndex = currentPage * weeksPerPage
        val endIndex = (startIndex + weeksPerPage).coerceAtMost(weeks.size)
        val detailWeeks = weeks.subList(startIndex, endIndex)

        val weeksWithEmptySlots = if (detailWeeks.size < weeksPerPage) {
            detailWeeks + List(weeksPerPage - detailWeeks.size) { null }
        } else {
            detailWeeks
        }

        WeeklyDensityChart(
            weeks = weeksWithEmptySlots,
            getColorForActivity = getColorForActivity,
            activityName = activityName,
            maxWeeks = weeksPerPage,
            showScale = true,
            showWeekNumbers = true,
            firstWeekNumber = startIndex + 1,
            chartHeight = 152.dp,
            isActivityHidden = { activityName ->
                viewModel.getActivityTemplateIsHidden(activityName)
            }
        )

    }
}