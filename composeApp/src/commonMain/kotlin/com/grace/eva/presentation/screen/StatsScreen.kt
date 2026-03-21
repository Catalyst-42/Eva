package com.grace.eva.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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
import com.grace.eva.presentation.component.ActivitiesBarChart
import com.grace.eva.presentation.component.ChartSegment
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.ui.theme.tracker.TemplateColors
import com.grace.eva.utils.formatDuration
import com.grace.eva.utils.parseColor
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// Data class for activity statistics
data class ActivityStat(
    val name: String,
    val totalDuration: Duration,
    val count: Int,
    val minDuration: Duration,
    val maxDuration: Duration
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

    // Sort activities by begin time for calculating durations
    val sortedActivities = remember(activities) {
        activities.sortedBy { it.begin }
    }

    // Calculate detailed statistics for each activity
    val activityStats = remember(sortedActivities, currentTime, currentSave) {
        val activitiesWithDuration = sortedActivities.mapIndexed { index, activity ->
            val endTime = when {
                index < sortedActivities.lastIndex -> sortedActivities[index + 1].begin
                isSaveCompleted -> currentSave!!.end
                else -> currentTime
            }
            val duration = endTime - activity.begin
            activity.name to duration
        }

        activitiesWithDuration
            .groupBy { it.first }
            .map { (name, durations) ->
                val totalSeconds = durations.sumOf { it.second.inWholeSeconds }.seconds
                val count = durations.size
                val minDuration = durations.minOf { it.second }
                val maxDuration = durations.maxOf { it.second }
                ActivityStat(name, totalSeconds, count, minDuration, maxDuration)
            }
            .filter { it.totalDuration > Duration.ZERO }
            .sortedByDescending { it.totalDuration.inWholeSeconds }
    }

    val totalDuration = if (isSaveCompleted && sortedActivities.isNotEmpty()) {
        currentSave!!.end - sortedActivities.first().begin
    } else {
        activityStats.sumOf { it.totalDuration.inWholeSeconds }.seconds
    }

    val totalActivities = remember(activityStats) {
        activityStats.sumOf { it.count }
    }

    // Calculate number of days in the save period
    val totalDays = remember(currentSave, sortedActivities) {
        if (currentSave != null && sortedActivities.isNotEmpty()) {
            val startTime = sortedActivities.first().begin
            val endTime = currentSave.end ?: currentTime
            val days = (endTime - startTime).inWholeDays.toInt()
            if (days > 0) days else 1
        } else {
            1
        }
    }

    // Calculate overall statistics
    val overallStat = remember(activityStats, totalDuration, totalActivities) {
        ActivityStat(
            name = "Все этапы",
            totalDuration = totalDuration,
            count = totalActivities,
            minDuration = activityStats.minOfOrNull { it.minDuration } ?: Duration.ZERO,
            maxDuration = activityStats.maxOfOrNull { it.maxDuration } ?: Duration.ZERO
        )
    }

    // Helper function to get color for activity
    fun getColorForActivity(name: String, index: Int = 0): Color {
        activityColors[name]?.let { return it }
        parseColor(TemplateColors.getColorForIndex(index))?.let { return it }
        return Color(0xFF2196F3)
    }

    // Prepare chart data for bar chart
    val chartData = remember(activityStats, activityColors) {
        activityStats.mapIndexed { index, stat ->
            ChartSegment(
                name = stat.name,
                duration = stat.totalDuration,
                color = getColorForActivity(stat.name, index)
            )
        }
    }

    // State for selected activity (null means all activities selected)
    var selectedActivityName by remember { mutableStateOf<String?>("Все этапы") }

    // Find selected activity stat
    val selectedStat = if (selectedActivityName == "Все этапы") {
        overallStat
    } else {
        activityStats.find { it.name == selectedActivityName }
    }

    // Prepare activity buttons in 2-column grid
    val buttonRows = remember(activityStats) {
        activityStats.chunked(2)
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
            // Density section
            item {
                DensitySection(
                    chartData = chartData,
                    totalActivities = totalActivities
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
                    overallDuration = totalDuration,
                    getColorForActivity = { name, index -> getColorForActivity(name, index) }
                )
            }

            // Statistics card for selected activity
            if (selectedStat != null) {
                item {
                    StatisticsCard(
                        stat = selectedStat,
                        isOverall = selectedActivityName == "Все этапы",
                        totalDays = totalDays
                    )
                }
            }
        }
    }
}

@Composable
fun DensitySection(
    chartData: List<ChartSegment>,
    totalActivities: Int
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
    overallDuration: Duration,
    getColorForActivity: (String, Int) -> Color
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

        ActivitySelectionButton(
            name = "Все этапы",
            color = MaterialTheme.colorScheme.outlineVariant,
            percentage = 0f,
            totalDuration = overallDuration,
            isSelected = selectedActivityName == "Все этапы",
            onClick = { onActivitySelected("Все этапы") },
            modifier = Modifier.fillMaxWidth(),
            isAllActivities = true
        )

        // Buttons for selected activities
        buttonRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { stat ->
                    val index = activityStats.indexOf(stat)
                    val isSelected = selectedActivityName == stat.name
                    val percentage = (stat.totalDuration.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()) * 100

                    ActivitySelectionButton(
                        name = stat.name,
                        color = getColorForActivity(stat.name, index),
                        percentage = percentage,
                        totalDuration = null,
                        isSelected = isSelected,
                        onClick = { onActivitySelected(stat.name) },
                        modifier = Modifier.weight(1f),
                        isAllActivities = false
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

@Composable
fun ActivitySelectionButton(
    name: String,
    color: Color,
    percentage: Float,
    totalDuration: Duration?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAllActivities: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer,
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
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(color)
            )
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Helper function for KMP-compatible percentage formatting
private fun formatPercentage(value: Float): String {
    val intPart = value.toInt()
    val fractional = ((value - intPart) * 10).toInt()
    return if (fractional == 0) "$intPart%" else "$intPart.$fractional%"
}

@Composable
fun StatisticsCard(
    stat: ActivityStat,
    isOverall: Boolean = false,
    totalDays: Int = 1
) {
    // Calculate average per day: total duration divided by number of days
    val averagePerDay = if (totalDays > 0) stat.totalDuration / totalDays else Duration.ZERO
    // Calculate average per week: average per day multiplied by 7
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
                text = if (isOverall) "Статистика по всем этапам" else "Статистика: ${stat.name}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Basic statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Общее время:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(stat.totalDuration), style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Количество этапов:", style = MaterialTheme.typography.bodyMedium)
                Text("${stat.count}", style = MaterialTheme.typography.bodyMedium)
            }

            // Average per occurrence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Среднее время на этап:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(stat.totalDuration / stat.count), style = MaterialTheme.typography.bodyMedium)
            }

            // Average per day and per week
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Среднее время в день:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(averagePerDay), style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Среднее время в неделю:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(averagePerWeek), style = MaterialTheme.typography.bodyMedium)
            }

            // Min/Max statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Минимальное время:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(stat.minDuration), style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Максимальное время:", style = MaterialTheme.typography.bodyMedium)
                Text(formatDuration(stat.maxDuration), style = MaterialTheme.typography.bodyMedium)
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