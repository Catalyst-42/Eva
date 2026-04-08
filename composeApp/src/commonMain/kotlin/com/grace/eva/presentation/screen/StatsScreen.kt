package com.grace.eva.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableDefaults.overscrollEffect
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.presentation.component.ActivityIcon
import com.grace.eva.presentation.component.chart.ActivitiesBarChart
import com.grace.eva.presentation.component.chart.ChartSegment
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.ui.theme.tracker.TemplateColors
import com.grace.eva.util.formatDuration
import com.grace.eva.util.formatFloat
import com.grace.eva.util.parseColor
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class ActivityStat(
    val name: String,
    val totalDuration: Duration,
    val count: Int,
    val minDuration: Duration,
    val maxDuration: Duration,
    val firstOccurrenceIndex: Int
)

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

    // Calculate activity durations first, then exclude hidden activities from stats
    val visibleActivitiesWithDuration = remember(activities, currentTime, currentSave, state.activityTemplates) {
        val activitiesWithDuration = activities.mapIndexed { index, activity ->
            val endTime = when {
                index < activities.lastIndex -> activities[index + 1].begin
                isSaveCompleted -> currentSave.end
                else -> currentTime
            }
            val duration = endTime - activity.begin
            activity.name to duration
        }

        activitiesWithDuration.filter { (name, duration) ->
            duration > Duration.ZERO && !viewModel.getActivityTemplateIsHidden(name)
        }
    }

    // Group by activity name and calculate stats, preserving first visible occurrence order
    val activityStats = remember(visibleActivitiesWithDuration) {
        val statsMap = mutableMapOf<String, MutableList<Duration>>()
        val firstOccurrence = mutableMapOf<String, Int>()

        visibleActivitiesWithDuration.forEachIndexed { index, (name, duration) ->
            statsMap.getOrPut(name) { mutableListOf() }.add(duration)
            if (!firstOccurrence.containsKey(name)) {
                firstOccurrence[name] = index
            }
        }

        statsMap.map { (name, durations) ->
            val totalSeconds = durations.sumOf { it.inWholeSeconds }.seconds
            val count = durations.size
            val minDuration = durations.minOrNull() ?: Duration.ZERO
            val maxDuration = durations.maxOrNull() ?: Duration.ZERO
            ActivityStat(
                name = name,
                totalDuration = totalSeconds,
                count = count,
                minDuration = minDuration,
                maxDuration = maxDuration,
                firstOccurrenceIndex = firstOccurrence[name] ?: Int.MAX_VALUE
            )
        }
            .sortedBy { it.firstOccurrenceIndex }
    }

    val totalDuration = remember(activityStats) {
        activityStats.sumOf { it.totalDuration.inWholeSeconds }.seconds
    }

    val totalActivities = remember(activityStats) {
        activityStats.sumOf { it.count }
    }

    // Calculate number of days in the save period
    val totalDays = remember(currentSave, activities, currentTime) {
        if (currentSave != null && activities.isNotEmpty()) {
            val startTime = activities.first().begin
            val endTime = currentSave.end ?: currentTime
            val totalSeconds = (endTime - startTime).inWholeSeconds.toDouble()
            if (totalSeconds > 0) totalSeconds / 86400.0 else 1.0
        } else {
            1.0
        }
    }

    // Helper function to get color for activity
    fun getColorForActivity(name: String, index: Int = 0): Color {
        activityColors[name]?.let { return it }
        parseColor(TemplateColors.getColorForIndex(index))?.let { return it }
        return Color(0xFF2196F3)
    }

    // Prepare chart data in the same order as activities appear
    val chartData = remember(activityStats, activityColors) {
        activityStats.mapIndexed { index, stat ->
            ChartSegment(
                name = stat.name,
                duration = stat.totalDuration,
                color = getColorForActivity(stat.name, index)
            )
        }
    }

    // State for selected activity
    var selectedActivityName by remember { mutableStateOf<String?>(activityStats.firstOrNull()?.name) }
    LaunchedEffect(activityStats) {
        if (selectedActivityName == null || activityStats.none { it.name == selectedActivityName }) {
            selectedActivityName = activityStats.firstOrNull()?.name
        }
    }

    // Find selected activity stat
    val selectedStat = activityStats.find { it.name == selectedActivityName }

    // Calculate percentage for selected activity
    val selectedPercentage = remember(selectedStat, totalDuration) {
        if (selectedStat != null && totalDuration.inWholeSeconds > 0) {
            (selectedStat.totalDuration.inWholeSeconds.toDouble() / totalDuration.inWholeSeconds.toDouble()) * 100
        } else {
            0.0
        }
    }

    // Prepare activity buttons in 2-column grid, preserving order
    val buttonRows = remember(activityStats) {
        activityStats.chunked(2)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
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
            // Density section
            item {
                DensitySection(
                    chartData = chartData,
                    totalActivities = totalActivities,
                    totalSaveDuration = totalDuration
                )
            }

            // Activity selection section
            item {
                ActivitySelectionSection(
                    selectedActivityName = selectedActivityName,
                    onActivitySelected = { selectedActivityName = it },
                    activityStats = activityStats,
                    buttonRows = buttonRows,
                    totalDuration = totalDuration,
                    getColorForActivity = { name, index -> getColorForActivity(name, index) },
                    viewModel = viewModel
                )
            }

            // Statistics card for selected activity
            if (selectedStat != null) {
                item {
                    StatisticsCard(
                        stat = selectedStat,
                        totalDays = totalDays,
                        totalDuration = totalDuration,
                        percentage = selectedPercentage
                    )
                }
            }
        }
    }
}

@Composable
fun DensitySection(
    chartData: List<ChartSegment>,
    totalActivities: Int,
    totalSaveDuration: Duration
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Плотность этапов",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        ActivitiesBarChart(
            data = chartData,
            totalActivities = totalActivities,
            totalDuration = totalSaveDuration,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ActivitySelectionSection(
    selectedActivityName: String?,
    onActivitySelected: (String) -> Unit,
    activityStats: List<ActivityStat>,
    buttonRows: List<List<ActivityStat>>,
    totalDuration: Duration,
    getColorForActivity: (String, Int) -> Color,
    viewModel: TrackerViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Выберите этап",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp)
        ) {
            val overscrollEffect = rememberOverscrollEffect()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState(),
                        overscrollEffect = overscrollEffect
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                buttonRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { stat ->
                            val index = activityStats.indexOf(stat)
                            val isSelected = selectedActivityName == stat.name
                            val percentage = if (totalDuration.inWholeSeconds > 0) {
                                (stat.totalDuration.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()) * 100
                            } else {
                                0f
                            }

                            ActivitySelectionButton(
                                name = stat.name,
                                color = getColorForActivity(stat.name, index),
                                percentage = percentage,
                                isSelected = isSelected,
                                onClick = { onActivitySelected(stat.name) },
                                modifier = Modifier.weight(1f),
                                viewModel = viewModel
                            )
                        }

                        // Fill empty space if only one item in row
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivitySelectionButton(
    name: String,
    color: Color,
    percentage: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackerViewModel
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = if (isSelected)
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        else
            null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ActivityIcon(viewModel.getTemplateForActivity(name) ?: ActivityTemplate())
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatisticsCard(
    stat: ActivityStat,
    totalDays: Double,
    totalDuration: Duration,
    percentage: Double
) {
    // Calculate average duration per occurrence
    val averageDurationPerOccurrence = if (stat.count > 0) {
        stat.totalDuration / stat.count
    } else {
        Duration.ZERO
    }

    // Calculate average time per day based on actual days and total time
    val averagePerDay = if (totalDays > 0) {
        stat.totalDuration / totalDays
    } else {
        Duration.ZERO
    }

    // Calculate average time per week (7 days)
    val averagePerWeek = averagePerDay * 7

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stat.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Number of occurrences
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Число этапов:", style = MaterialTheme.typography.bodyMedium)
                Text("${stat.count}", style = MaterialTheme.typography.bodyMedium)
            }

            // Total time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Общее время:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(stat.totalDuration), style = MaterialTheme.typography.bodyMedium)
            }

            // Percentage of total time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Плотность:", style = MaterialTheme.typography.bodyMedium)
                Text("${formatFloat(percentage.toFloat(), 1)}%", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.padding(horizontal = 8.dp))

            // Average per week
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("В среднем в неделю:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(averagePerWeek), style = MaterialTheme.typography.bodyMedium)
            }

            // Average per day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("В среднем в день:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(averagePerDay), style = MaterialTheme.typography.bodyMedium)
            }

            // Average duration per occurrence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("В среднем за этап:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(averageDurationPerOccurrence), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.padding(horizontal = 8.dp))

            // Max duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Максимальная длина этапа:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(stat.maxDuration), style = MaterialTheme.typography.bodyMedium)
            }

            // Min duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Минимальная длина этапа:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(stat.minDuration), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.LARGE))
    }
    StatsScreenContent(mockViewModel)
}

@Preview(showBackground = true)
@Composable
fun StatsScreenEmptyPreview() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.EMPTY))
    }
    StatsScreenContent(mockViewModel)
}
