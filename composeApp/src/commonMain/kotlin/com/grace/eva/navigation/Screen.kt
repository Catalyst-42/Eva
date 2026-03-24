package com.grace.eva.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.HorizontalSplit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.sharp.Save
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val order: Int
) {
    Tracker("tracker", Icons.Rounded.Gamepad, "Трекер", 0),
    Activities("activities", Icons.Default.ViewAgenda, "Активности", 1),
    Stats("stats", Icons.Rounded.Dashboard, "Статистика", 2),
    Settings("settings", Icons.Default.Save, "Сохранения", 3)
}
