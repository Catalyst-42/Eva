package com.grace.eva.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Текущее время обновляется каждую секунду
    var currentTime by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Clock.System.now()
            delay(1000L)
        }
    }

    // Статистика пересчитывается при изменении activities или currentTime
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

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            // Pie chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val outlineColor = MaterialTheme.colorScheme.surface

                    Canvas(
                        modifier = Modifier.size(80.dp)
                    ) {
                        var startAngle = -90f
                        val values = activityStats.map { it.second.first.inWholeSeconds.toFloat() }
                        val total = values.sum()

                        values.forEachIndexed { index, value ->
                            val sweepAngle = (value / total * 360f)

                            drawArc(
                                color = chartColors[index % chartColors.size],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )

                            drawArc(
                                color = outlineColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            startAngle += sweepAngle
                        }
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activityStats) { (name, stats) ->
                    val (duration, count) = stats
                    StatsLegendCard(
                        name = name,
                        duration = duration,
                        count = count,
                        totalDuration = totalDuration,
                        color = chartColors[activityStats.indexOfFirst { it.first == name } % chartColors.size]
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Всего активностей: ${activityStats.sumOf { it.second.second }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = formatDuration(totalDuration),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
    val percentage = (duration.inWholeSeconds * 100 / totalDuration.inWholeSeconds).toInt()

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

            Spacer(modifier = Modifier.size(12.dp))

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
                        text = "Повторов: $count",
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