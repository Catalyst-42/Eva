package com.grace.eva.presentation.screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
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
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Bar chart canvas
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

                data.forEachIndexed { index, segment ->
                    val weight = segment.duration.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()
                    val segmentWidth = barWidth * weight

                    drawRect(
                        color = segment.color,
                        topLeft = Offset(startX, 0f),
                        size = androidx.compose.ui.geometry.Size(segmentWidth, size.height)
                    )

                    if (index < data.lastIndex) {
                        drawLine(
                            color = lineColor,
                            start = Offset(startX + segmentWidth, 0f),
                            end = Offset(startX + segmentWidth, size.height),
                            strokeWidth = 4.dp.toPx()
                        )
                    }

                    startX += segmentWidth
                }

                drawRect(
                    color = borderColor,
                    size = size,
                    style = Stroke(width = 1.dp.toPx())
                )
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