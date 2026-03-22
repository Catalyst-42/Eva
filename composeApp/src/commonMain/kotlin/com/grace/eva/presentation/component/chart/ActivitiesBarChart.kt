package com.grace.eva.presentation.component.chart

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grace.eva.util.formatDuration
import com.grace.eva.util.formatFloat
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

    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        color = Color.Black,
        fontSize = 12.sp
    )

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
            // Bar chart canvas with percentage labels
            val outlineColor = Color.Black
            val backgroundColor = MaterialTheme.colorScheme.surface

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(backgroundColor)
            ) {
                val barWidth = size.width
                var startX = 0f

                data.forEachIndexed { index, segment ->
                    val weight = segment.duration.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()
                    val segmentWidth = barWidth * weight
                    val percentage = (segment.duration.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()) * 100

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

                    // Draw percentage text if segment width is large enough (>5% of total width)
                    if (percentage > 10f) {
                        val percentageText = formatFloat(percentage, 1) + "%"
                        val textLayoutResult = textMeasurer.measure(
                            text = percentageText,
                            style = textStyle
                        )
                        val textWidth = textLayoutResult.size.width
                        val textHeight = textLayoutResult.size.height

                        val textX = startX + (segmentWidth / 2) - (textWidth / 2)
                        val textY = (size.height / 2) - (textHeight / 2)

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(textX, textY)
                        )
                    }

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
            // Preview 1: Multiple activities with percentage labels
            ActivitiesBarChart(
                data = listOf(
                    ChartSegment("Работа", 7200.seconds, Color(0xFF4CAF50)),      // ~36%
                    ChartSegment("Отдых", 3600.seconds, Color(0xFF2196F3)),       // ~18%
                    ChartSegment("Спорт", 1800.seconds, Color(0xFFFF9800)),       // ~9%
                    ChartSegment("Обучение", 2700.seconds, Color(0xFF9C27B0))     // ~13.5%
                ),
                totalActivities = 12,
                modifier = Modifier.fillMaxWidth()
            )

            // Preview 2: Two activities only
            ActivitiesBarChart(
                data = listOf(
                    ChartSegment("Работа", 5400.seconds, Color(0xFF4CAF50)),      // ~60%
                    ChartSegment("Отдых", 3600.seconds, Color(0xFF2196F3))        // ~40%
                ),
                totalActivities = 8,
                modifier = Modifier.fillMaxWidth()
            )

            // Preview 3: Single activity
            ActivitiesBarChart(
                data = listOf(
                    ChartSegment("Работа", 7200.seconds, Color(0xFF4CAF50))       // 100%
                ),
                totalActivities = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // Preview 4: With small segments (<5%) - no labels on small ones
            ActivitiesBarChart(
                data = listOf(
                    ChartSegment("Работа", 7200.seconds, Color(0xFF4CAF50)),      // ~50%
                    ChartSegment("Отдых", 3600.seconds, Color(0xFF2196F3)),       // ~25%
                    ChartSegment("Спорт", 1800.seconds, Color(0xFFFF9800)),       // ~12.5%
                    ChartSegment("Обучение", 900.seconds, Color(0xFF9C27B0)),     // ~6.25%
                    ChartSegment("Мелкие дела", 100.seconds, Color(0xFFE91E63))   // ~0.7% - no label
                ),
                totalActivities = 20,
                modifier = Modifier.fillMaxWidth()
            )

            // Preview 5: Without total activities count
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

            // Preview 6: Without stats row
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