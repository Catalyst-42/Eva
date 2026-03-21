package com.grace.eva.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.presentation.screen.components.*
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.ui.theme.tracker.TemplateColors
import com.grace.eva.utils.parseColor
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// Default fallback color if parsing fails
private val DEFAULT_FALLBACK_COLOR = Color(0xFF2196F3)

@Composable
fun StatsScreen(
    appContainer: AppContainer
) {
    val viewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModel.Factory(appContainer)
    )
    StatsScreenContent(viewModel)
}

@Composable
fun StatsScreenContent(viewModel: TrackerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val currentSave = state.currentSave
    val activities = currentSave?.activities ?: emptyList()
    val isSaveCompleted = currentSave?.end != null
    val templates = state.activityTemplates

    // Create mapping of activity names to colors from templates
    val activityColors = remember(templates) {
        templates.mapNotNull { template ->
            parseColor(template.color)?.let { color ->
                template.name to color
            }
        }.toMap()
    }

    var currentTime by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) {
        if (!isSaveCompleted) {
            while (true) {
                currentTime = Clock.System.now()
                delay(1000L)
            }
        }
    }

    // Sort activities by begin time for calculating durations
    val sortedActivities = remember(activities) {
        activities.sortedBy { it.begin }
    }

    val activityStats = remember(sortedActivities, currentTime, currentSave) {
        // Calculate durations using next activity's begin time or save end
        val activitiesWithDuration = sortedActivities.mapIndexed { index, activity ->
            val endTime = when {
                index < sortedActivities.lastIndex -> sortedActivities[index + 1].begin
                isSaveCompleted -> currentSave.end
                else -> currentTime
            }
            val duration = endTime - activity.begin
            activity.name to duration
        }

        // Group by name and sum durations
        activitiesWithDuration
            .groupBy { it.first }
            .mapValues { entry ->
                val totalSeconds = entry.value.sumOf { it.second.inWholeSeconds }.seconds
                val count = entry.value.size
                Pair(totalSeconds, count)
            }
            .filter { it.value.first > Duration.ZERO }
            .toList()
    }

    val totalDuration = if (isSaveCompleted && sortedActivities.isNotEmpty()) {
        currentSave.end - sortedActivities.first().begin
    } else {
        activityStats.sumOf { it.second.first.inWholeSeconds }.seconds
    }

    val totalActivities = remember(activityStats) {
        activityStats.sumOf { it.second.second }
    }

    // Helper function to get color for activity
    fun getColorForActivity(name: String, index: Int = 0): Color {
        // First try to get color from template
        activityColors[name]?.let { return it }

        // If not found, try to parse default color from TemplateColors
        parseColor(TemplateColors.getColorForIndex(index))?.let { return it }

        // If all fails, return default fallback color
        return DEFAULT_FALLBACK_COLOR
    }

    // Prepare chart data for bar chart
    val chartData = remember(activityStats, activityColors) {
        activityStats.mapIndexed { index, (name, stats) ->
            val (duration, _) = stats
            ChartSegment(
                name = name,
                duration = duration,
                color = getColorForActivity(name, index)
            )
        }
    }

    // Prepare legend items
    val legendItems = remember(activityStats, totalDuration, activityColors) {
        activityStats.mapIndexed { index, (name, stats) ->
            val (duration, count) = stats
            LegendItem(
                name = name,
                duration = duration,
                count = count,
                totalDuration = totalDuration,
                color = getColorForActivity(name, index)
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentSave == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Нет активного сохранения",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (activityStats.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Нет данных для отображения",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "Легенда",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                LegendTable(
                    items = legendItems,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Плотность этапов",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ActivitiesBarChart(
                    data = chartData,
                    totalActivities = totalActivities,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStatsScreen() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.LARGE))
    }
    StatsScreenContent(mockViewModel)
}

@Preview(showBackground = true)
@Composable
fun PreviewStatsScreen_NoSave() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.EMPTY))
    }
    StatsScreenContent(mockViewModel)
}