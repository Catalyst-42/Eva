package com.grace.eva.ui.theme.tracker

import androidx.compose.ui.graphics.Color

object TemplateColors {
    val defaultColors = listOf(
        "#2196F3", // Blue
        "#FF9800", // Orange
        "#F44336", // Red
        "#9C27B0", // Purple
        "#4CAF50", // Green
        "#00BCD4", // Cyan
        "#FFC107", // Amber
        "#3F51B5", // Indigo
        "#E91E63", // Pink
        "#009688"  // Teal
    )

    fun getColorForIndex(index: Int): String {
        return defaultColors[index % defaultColors.size]
    }

    fun getDefaultColor(): Color {
        return Color(0xFF2196F3)
    }
}