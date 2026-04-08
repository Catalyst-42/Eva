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
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.presentation.viewmodel.TrackerViewModel
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
    totalDuration: Duration,
    modifier: Modifier = Modifier,
    showStatsRow: Boolean = true
) {
    if (data.isEmpty() || totalDuration <= Duration.ZERO) return

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

                data.forEach { segment ->
                    val weight = segment.duration.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()
                    val segmentWidth = barWidth * weight
                    val percentage = (segment.duration.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()) * 100

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
private fun ActivitiesBarChartPreview() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }

    val currentSave = mockViewModel.uiState.value.currentSave
    val activities = currentSave?.activities ?: emptyList()
    val chartData = activities.groupBy { it.name }.map { (name, list) ->
        ChartSegment(
            name = name,
            duration = (list.size * 3600).seconds,
            color = mockViewModel.getColorForActivity(name)
        )
    }
    val totalDuration = chartData.sumOf { it.duration.inWholeSeconds }.seconds

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ActivitiesBarChart(
                data = chartData,
                totalActivities = activities.size,
                totalDuration = totalDuration,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
