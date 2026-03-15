package com.grace.eva.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.utils.formatDuration
import com.grace.eva.utils.formatFloat
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
    val activities = state.activities.activities

    var currentTime by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Clock.System.now()
            delay(1000L)
        }
    }

    val activityStats = remember(activities, currentTime) {
        activities
            .groupBy { it.name }
            .mapValues { entry ->
                val totalSeconds = entry.value.sumOf { activity ->
                    val end = activity.end ?: currentTime
                    (end - activity.begin).inWholeSeconds
                }.seconds
                val count = entry.value.size
                Pair(totalSeconds, count)
            }
            .filter { it.value.first > Duration.ZERO }
            .toList()
    }

    val totalDuration = remember(activityStats) {
        activityStats.sumOf { it.second.first.inWholeSeconds }.seconds
    }

    val totalActivities = remember(activityStats) {
        activityStats.sumOf { it.second.second }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (activityStats.isEmpty()) {
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
        } else {
            Text(
                text = "Плотность этапов",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Horizontal bar chart with total stats below
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Bar chart
                    val lineColor = MaterialTheme.colorScheme.surface
                    val borderColor = MaterialTheme.colorScheme.outlineVariant

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        val barWidth = size.width
                        var startX = 0f

                        activityStats.forEachIndexed { index, (_, stats) ->
                            val weight = stats.first.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()
                            val segmentWidth = barWidth * weight

                            drawRect(
                                color = chartColors[index % chartColors.size],
                                topLeft = Offset(startX, 0f),
                                size = androidx.compose.ui.geometry.Size(segmentWidth, size.height)
                            )

                            // Draw separator
                            if (index < activityStats.lastIndex) {
                                drawLine(
                                    color = lineColor,
                                    start = Offset(startX + segmentWidth, 0f),
                                    end = Offset(startX + segmentWidth, size.height),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }

                            startX += segmentWidth
                        }

                        // Draw border
                        drawRect(
                            color = borderColor,
                            size = size,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Total stats below the bar chart (replacing the separate card)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Всего этапов: $totalActivities",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = formatDuration(totalDuration),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Легенда",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            activityStats.forEach { (name, stats) ->
                val (duration, count) = stats
                StatsLegendCard(
                    name = name,
                    duration = duration,
                    count = count,
                    totalDuration = totalDuration,
                    color = chartColors[activityStats.indexOfFirst { it.first == name } % chartColors.size],
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun StatsLegendCard(
    name: String,
    duration: Duration,
    count: Int,
    totalDuration: Duration,
    color: Color,
    modifier: Modifier = Modifier
) {
    val percentage = formatFloat(
        duration.inWholeSeconds * 100f / totalDuration.inWholeSeconds,
        2
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = color
                ),
                shape = MaterialTheme.shapes.small
            ) {}

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Этапов: $count",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private val chartColors = listOf(
    Color(0xFF4CAF50), // Green
    Color(0xFF2196F3), // Blue
    Color(0xFFFF9800), // Orange
    Color(0xFF9C27B0), // Purple
    Color(0xFFF44336), // Red
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFFC107), // Amber
    Color(0xFF3F51B5), // Indigo
    Color(0xFFE91E63), // Pink
    Color(0xFF009688)  // Teal
)

@Preview(showBackground = true)
@Composable
fun PreviewStatsScreen() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.LARGE))
    }
    StatsScreenContent(mockViewModel)
}