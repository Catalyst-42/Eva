package com.grace.eva.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun ColorLegend(
    items: List<LegendItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { item ->
                    LegendColorButton(item, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LegendColorButton(item: LegendItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(item.color)
        )
        Text(
            text = item.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewColorLegend() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ColorLegend(
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
                        name = "Сон",
                        duration = 28800.seconds,
                        count = 1,
                        totalDuration = 86400.seconds,
                        color = Color(0xFF4CAF50)
                    ),
                    LegendItem(
                        name = "Обучение",
                        duration = 10800.seconds,
                        count = 2,
                        totalDuration = 86400.seconds,
                        color = Color(0xFF9C27B0)
                    )
                )
            )
        }
    }
}