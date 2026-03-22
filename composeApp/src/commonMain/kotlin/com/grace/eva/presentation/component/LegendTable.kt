package com.grace.eva.presentation.screen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grace.eva.util.formatDuration
import com.grace.eva.util.formatFloat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class LegendItem(
    val name: String,
    val duration: Duration,
    val count: Int,
    val totalDuration: Duration,
    val color: Color
)

@Composable
fun LegendTable(
    items: List<LegendItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            LegendCard(item)
        }
    }
}

@Composable
private fun LegendCard(item: LegendItem) {
    val percentage = formatFloat(
        item.duration.inWholeSeconds * 100f / item.totalDuration.inWholeSeconds,
        1
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: color indicator and name (right side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom row: count and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Этапов: ${item.count}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatDuration(item.duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLegendTable() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            LegendTable(
                items = listOf(
                    LegendItem(
                        name = "Работа",
                        duration = 14400.seconds,
                        count = 3,
                        totalDuration = 86400.seconds,
                        color = Color(0xFF2196F3)
                    ),
                    LegendItem(
                        name = "Отдых",
                        duration = 7200.seconds,
                        count = 5,
                        totalDuration = 86400.seconds,
                        color = Color(0xFFFF9800)
                    ),
                    LegendItem(
                        name = "Обучение",
                        duration = 10800.seconds,
                        count = 2,
                        totalDuration = 86400.seconds,
                        color = Color(0xFF4CAF50)
                    )
                )
            )
        }
    }
}