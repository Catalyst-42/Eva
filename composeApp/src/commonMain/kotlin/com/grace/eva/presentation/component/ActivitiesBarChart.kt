package com.grace.eva.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grace.eva.utils.formatDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class ChartSegment(
    val name: String,
    val duration: Duration,
    val color: Color
)

@Composable
fun ActivitiesBarChart(
    data: List<ChartSegment>,
    totalActivities: Int? = null,
    modifier: Modifier = Modifier,
    showStatsRow: Boolean = true
) {
    // Calculate total duration from provided data
    val totalDuration = remember(data) {
        data.sumOf { it.duration.inWholeSeconds }.seconds
    }

    if (data.isEmpty()) return

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Bar chart canvas
            val outlineColor = Color.Black
            val backgroundColor = MaterialTheme.colorScheme.surface

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(backgroundColor)
            ) {
                val barWidth = size.width
                var startX = 0f

                data.forEachIndexed { index, segment ->
                    val weight = segment.duration.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()
                    val segmentWidth = barWidth * weight

                    // Draw the bar segment
                    drawRect(
                        color = segment.color,
                        topLeft = Offset(startX, 0f),
                        size = Size(segmentWidth, size.height)
                    )

                    drawRect(
                        color = outlineColor,
                        topLeft = Offset(startX, 0f),
                        size = Size(segmentWidth, size.height),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    startX += segmentWidth
                }
            }

            if (showStatsRow) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (totalActivities != null) {
                        Text(
                            text = "Всего этапов: $totalActivities",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

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
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewActivitiesBarChart() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview 1: Multiple activities
            ActivitiesBarChart(
                data = listOf(
                    ChartSegment("Работа", 7200.seconds, Color(0xFF4CAF50)),
                    ChartSegment("Отдых", 3600.seconds, Color(0xFF2196F3)),
                    ChartSegment("Спорт", 1800.seconds, Color(0xFFFF9800)),
                    ChartSegment("Обучение", 2700.seconds, Color(0xFF9C27B0))
                ),
                totalActivities = 12,
                modifier = Modifier.fillMaxWidth()
            )

            // Preview 2: Two activities only
            ActivitiesBarChart(
                data = listOf(
                    ChartSegment("Работа", 5400.seconds, Color(0xFF4CAF50)),
                    ChartSegment("Отдых", 1800.seconds, Color(0xFF2196F3))
                ),
                totalActivities = 8,
                modifier = Modifier.fillMaxWidth()
            )

            // Preview 3: Single activity
            ActivitiesBarChart(
                data = listOf(
                    ChartSegment("Работа", 7200.seconds, Color(0xFF4CAF50))
                ),
                totalActivities = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // Preview 4: Without total activities count
            ActivitiesBarChart(
                data = listOf(
                    ChartSegment("Работа", 3600.seconds, Color(0xFF4CAF50)),
                    ChartSegment("Сон", 28800.seconds, Color(0xFF2196F3)),
                    ChartSegment("Еда", 1800.seconds, Color(0xFFFF9800))
                ),
                totalActivities = null,
                showStatsRow = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Preview 5: Without stats row
            ActivitiesBarChart(
                data = listOf(
                    ChartSegment("Работа", 3600.seconds, Color(0xFF4CAF50)),
                    ChartSegment("Сон", 28800.seconds, Color(0xFF2196F3))
                ),
                totalActivities = 10,
                showStatsRow = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
