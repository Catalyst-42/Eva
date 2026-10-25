package com.grace.eva.presentation.component.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grace.eva.presentation.statistics.WeeklyStatistics
import com.grace.eva.util.formatFloat

@Composable
fun WeeklyDensityChart(
    weeks: List<WeeklyStatistics?>?,
    getColorForActivity: (String, Int) -> Color,
    activityName: String? = null,
    maxWeeks: Int = Int.MAX_VALUE,
    showScale: Boolean = true,
    showWeekNumbers: Boolean = true,
    firstWeekNumber: Int? = null,
    backgroundLabel: String? = null,
    chartHeight: Dp = 152.dp,
    isActivityHidden: (String) -> Boolean = { false },
    backgroundLabelPosition: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    val validWeeks = weeks?.map { week ->
        week ?: WeeklyStatistics(
            weekStart = kotlinx.datetime.LocalDate(2024, 1, 1),
            totalDuration = kotlin.time.Duration.ZERO,
            weekDuration = kotlin.time.Duration.ZERO,
            activityDurations = emptyList()
        )
    } ?: emptyList()
    
    if (validWeeks.isEmpty()) return
    val visibleWeeks = validWeeks.takeLast(maxWeeks.coerceAtLeast(1))
    val firstVisibleWeekNumber = firstWeekNumber ?: (validWeeks.size - visibleWeeks.size + 1)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = MaterialTheme.colorScheme.surface
    val watermarkColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val textMeasurer = rememberTextMeasurer()
    val maxDensity = if (!showScale || activityName == null) {
        100.0
    } else {
        val observedMax = visibleWeeks.maxOfOrNull { week ->
            val duration = week.activityDurations
                .firstOrNull { it.activityName == activityName }
                ?.duration?.inWholeSeconds ?: 0L
            if (week.totalDuration > kotlin.time.Duration.ZERO) {
                duration.toDouble() / week.totalDuration.inWholeSeconds * 100.0
            } else {
                0.0
            }
        } ?: 0.0
        chartMaximum(observedMax)
    }
    val percentageTextStyle = TextStyle(
        color = Color.Black,
        fontSize = 10.sp,
        textAlign = TextAlign.Center
    )
    val gridTextStyle = TextStyle(
        color = gridColor,
        fontSize = 8.sp,
        textAlign = TextAlign.Center
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
            if (!showScale && backgroundLabel != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val watermarkStyle = TextStyle(
                        color = watermarkColor,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )
                    val layout = textMeasurer.measure(backgroundLabel, watermarkStyle)
                    val centerX = (size.width - layout.size.width) / 2f
                    val centerY = size.height * backgroundLabelPosition - (layout.size.height / 2f)
                    drawText(
                        layout,
                        topLeft = Offset(centerX, centerY)
                    )
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellGap = 0f
                val cellWidth = size.width / visibleWeeks.size
                visibleWeeks.forEachIndexed { weekIndex, week ->
                    val cellLeft = weekIndex * (cellWidth + cellGap)
                    if (showScale) {
                        val segments = week.activityDurations
                            .filter { 
                                (activityName == null || it.activityName == activityName) && 
                                !isActivityHidden(it.activityName)
                            }
                        var bottom = size.height
                        val segmentBounds = mutableListOf<Pair<Float, Float>>()
                        
                        segments.forEachIndexed { _, segment ->
                            val weekFraction = if (week.totalDuration > kotlin.time.Duration.ZERO) {
                                segment.duration.inWholeSeconds.toDouble() /
                                    week.totalDuration.inWholeSeconds.toDouble()
                            } else {
                                0.0
                            }
                            val density = weekFraction * 100.0
                            val displayedFraction = (density / maxDensity).coerceIn(0.0, 1.0)
                            val segmentHeight = size.height * displayedFraction.toFloat()
                            if (segmentHeight > 0f) {
                                bottom -= segmentHeight
                                segmentBounds.add(bottom to bottom + segmentHeight)
                            }
                        }

                        // Draw grid lines with overlap check
                        (0..4).forEach { line ->
                            val y = size.height * (4 - line) / 4f
                            
                            // Check overlap with segments
                            val isOverlapping = segmentBounds.any { (top, bottom) ->
                                y in (top - 1.dp.toPx())..(bottom + 1.dp.toPx())
                            }
                            
                            if (!isOverlapping) {
                                drawLine(
                                    color = gridColor,
                                    start = Offset(cellLeft, y),
                                    end = Offset(cellLeft + cellWidth, y),
                                    strokeWidth = 0.5.dp.toPx()
                                )
                                val label = formatFloat(
                                    (line * maxDensity / 4).toFloat(),
                                    if (maxDensity <= 10.0) 1 else 0
                                ) + "%"
                                val layout = textMeasurer.measure(label, gridTextStyle)
                                val centerX = cellLeft + cellWidth / 2f
                                val centerY = y - layout.size.height / 2f
                                val left = centerX - layout.size.width / 2f - 2.dp.toPx()
                                val right = centerX + layout.size.width / 2f + 2.dp.toPx()
                                val textY = centerY
                                drawRect(
                                    color = backgroundColor,
                                    topLeft = Offset(left, textY - 1.dp.toPx()),
                                    size = Size(right - left, layout.size.height + 2.dp.toPx())
                                )
                                drawText(
                                    textLayoutResult = layout,
                                    topLeft = Offset(centerX - layout.size.width / 2f, textY),
                                    color = gridColor
                                )
                            }
                        }
                    }

                    // Draw segments
                    val segments = week.activityDurations
                        .filter { 
                            (activityName == null || it.activityName == activityName) && 
                            !isActivityHidden(it.activityName)
                        }
                    var bottom = size.height
                    segments.forEachIndexed { segmentIndex, segment ->
                        val weekFraction = if (week.totalDuration > kotlin.time.Duration.ZERO) {
                            segment.duration.inWholeSeconds.toDouble() /
                                week.totalDuration.inWholeSeconds.toDouble()
                        } else {
                            0.0
                        }
                        val density = weekFraction * 100.0
                        val displayedFraction = (density / maxDensity).coerceIn(0.0, 1.0)
                        val segmentHeight = size.height * displayedFraction.toFloat()
                        if (segmentHeight > 0f) {
                            bottom -= segmentHeight
                            drawRect(
                                color = getColorForActivity(segment.activityName, segmentIndex),
                                topLeft = Offset(cellLeft, bottom),
                                size = Size(cellWidth, segmentHeight)
                            )
                            if (showScale) {
                                val cellLabel = formatFloat(density.toFloat(), 1) + "%"
                                val cellTextLayout = textMeasurer.measure(
                                    cellLabel,
                                    percentageTextStyle
                                )
                                if (cellTextLayout.size.width <= cellWidth &&
                                    cellTextLayout.size.height <= segmentHeight
                                ) {
                                    drawText(
                                        textLayoutResult = cellTextLayout,
                                        topLeft = Offset(
                                            cellLeft + (cellWidth - cellTextLayout.size.width) / 2f,
                                            bottom + (segmentHeight - cellTextLayout.size.height) / 2f
                                        )
                                    )
                                }
                            }
                            drawRect(
                                color = Color.Black.copy(alpha = 0.65f),
                                topLeft = Offset(cellLeft, bottom),
                                size = Size(cellWidth, segmentHeight),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                }
            }

        }

        if (showWeekNumbers) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                visibleWeeks.forEachIndexed { index, _ ->
                    Text(
                        text = (firstVisibleWeekNumber + index).toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun chartMaximum(value: Double): Double = when {
    value <= 1.0 -> 1.0
    value <= 2.0 -> 2.0
    value <= 5.0 -> 5.0
    value <= 10.0 -> 10.0
    value <= 20.0 -> 20.0
    value <= 50.0 -> 50.0
    else -> kotlin.math.ceil(value / 10.0) * 10.0
}