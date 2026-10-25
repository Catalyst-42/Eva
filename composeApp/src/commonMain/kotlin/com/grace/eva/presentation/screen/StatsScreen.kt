package com.grace.eva.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.presentation.component.ActivityIcon
import com.grace.eva.presentation.component.ActivitySelectionSection
import com.grace.eva.presentation.component.SaveDensityOverviewSection
import com.grace.eva.presentation.component.StatisticsCard
import com.grace.eva.presentation.component.WeeklyOverviewSection
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.ui.theme.tracker.TemplateColors
import com.grace.eva.util.parseColor

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
    val currentSave = state.currentSave
    val templates = state.activityTemplates
    val cachedSnapshot by viewModel.statisticsSnapshot.collectAsState()

    LaunchedEffect(currentSave?.id) {
        if (currentSave != null) viewModel.ensureStatisticsSnapshot()
    }
    val currentSnapshot = cachedSnapshot?.takeIf { it.saveId == currentSave?.id }
    val activityStats = currentSnapshot?.activityStats.orEmpty()
    val weeklyStatistics = currentSnapshot?.weeklyStatistics.orEmpty()
    val totalDays = currentSnapshot?.totalDays ?: 1.0

    // Create mapping of activity names to colors from templates
    val activityColors = remember(templates) {
        templates.mapNotNull { template ->
            parseColor(template.color)?.let { color ->
                template.name to color
            }
        }.toMap()
    }

    var selectedWeekNumber by remember(currentSave?.id) { mutableStateOf("") }
    val enteredWeekIndex = selectedWeekNumber.toIntOrNull()?.minus(1)
    val selectedWeekIndex = if (selectedWeekNumber.isBlank()) {
        weeklyStatistics.lastIndex
    } else {
        enteredWeekIndex?.takeIf { it in weeklyStatistics.indices } ?: -1
    }
    val selectedWeek = weeklyStatistics.getOrNull(selectedWeekIndex)

    // Helper function to get color for activity
    fun getColorForActivity(name: String, index: Int = 0): Color {
        activityColors[name]?.let { return it }
        parseColor(TemplateColors.getColorForIndex(index))?.let { return it }
        return Color(0xFF2196F3)
    }

    // Null selects all activities.
    var selectedActivityName by remember(currentSave?.id) { mutableStateOf<String?>(null) }
    LaunchedEffect(activityStats) {
        if (selectedActivityName != null && activityStats.none { it.name == selectedActivityName }) {
            selectedActivityName = null
        }
    }

    val selectedStat = activityStats.find { it.name == selectedActivityName }

    // Keep the selector in template order; preserve untemplated history entries afterward.
    val buttonRows = remember(activityStats, templates) {
        val statsByName = activityStats.associateBy { it.name }
        val templateNames = templates.map { it.name }.toSet()
        val templateOrderedStats = templates.mapNotNull { statsByName[it.name] }
        val untemplatedStats = activityStats
            .filterNot { it.name in templateNames }
            .sortedBy { it.firstOccurrenceIndex }
        (templateOrderedStats + untemplatedStats).chunked(2)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        overscrollEffect = null
    ) {
        if (currentSave == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Нет активного сохранения",
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (currentSnapshot == null) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (activityStats.isEmpty()) {
            item {
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
            }
        } else {
            item {
                SaveDensityOverviewSection(
                    weeks = weeklyStatistics,
                    activityName = selectedActivityName,
                    activityStats = activityStats,
                    chartHeight = state.tracker.saveDensityHeight,
                    onRefresh = {
                        viewModel.refreshStatisticsSnapshot()
                        selectedWeekNumber = ""
                    },
                    getColorForActivity = { name, _ ->
                        getColorForActivity(
                            name,
                            activityStats.indexOfFirst { it.name == name }.coerceAtLeast(0)
                        )
                    },
                    viewModel = viewModel
                )
            }
            item {
                ActivitySelectionSection(
                    selectedActivityName = selectedActivityName,
                    onActivitySelected = { selectedActivityName = it },
                    activityStats = activityStats,
                    buttonRows = buttonRows,
                    viewModel = viewModel
                )
            }

            item {
                WeeklyOverviewSection(
                    weeks = weeklyStatistics,
                    selectedWeekIndex = selectedWeekIndex,
                    activityName = selectedActivityName,
                    getColorForActivity = { name, _ ->
                        getColorForActivity(
                            name,
                            activityStats.indexOfFirst { it.name == name }.coerceAtLeast(0)
                        )
                    },
                    viewModel = viewModel,
                )
            }

            item {
                StatisticsCard(
                    stat = selectedStat,
                    totalDays = totalDays,
                    weeks = weeklyStatistics,
                    selectedWeek = selectedWeek,
                    selectedWeekIndex = selectedWeekIndex,
                    onWeekSelected = { index -> selectedWeekNumber = (index + 1).toString() }
                )
            }
        }
    }
}


@Composable
fun ActivitySelectionButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackerViewModel
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = if (isSelected)
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        else
            null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ActivityIcon(viewModel.getTemplateForActivity(name) ?: ActivityTemplate())
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.LARGE))
    }
    StatsScreenContent(mockViewModel)
}

@Preview(showBackground = true)
@Composable
fun StatsScreenEmptyPreview() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.EMPTY))
    }
    StatsScreenContent(mockViewModel)
}
