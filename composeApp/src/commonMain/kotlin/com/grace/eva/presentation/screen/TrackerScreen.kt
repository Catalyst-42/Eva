package com.grace.eva.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.presentation.component.ActivitiesMapChart
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.screen.floating.TemplateManagementScreen
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.utils.parseColor
import kotlinx.coroutines.delay

@Composable
fun TrackerScreen(
    appContainer: AppContainer,
) {
    val viewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModel.Factory(appContainer)
    )

    TrackerScreenContent(viewModel)
}

@Composable
fun TrackerScreenContent(viewModel: TrackerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val currentActivity by viewModel.currentActivity.collectAsState()
    val currentSave = state.currentSave
    val activityTemplates = state.activityTemplates

    // Save template management state
    var isManagingTemplates by rememberSaveable { mutableStateOf(false) }

    if (isManagingTemplates) {
        TemplateManagementScreen(
            activityTemplates = activityTemplates,
            viewModel = viewModel,
            onClose = { isManagingTemplates = false }
        )
    } else {
        MainTrackerContent(
            currentSave = currentSave,
            currentActivity = currentActivity,
            activityTemplates = activityTemplates,
            onManageTemplatesClick = { isManagingTemplates = true },
            viewModel = viewModel
        )
    }
}

@Composable
private fun MainTrackerContent(
    currentSave: com.grace.eva.domain.model.Save?,
    currentActivity: com.grace.eva.domain.model.Activity?,
    activityTemplates: List<com.grace.eva.domain.model.ActivityTemplate>,
    onManageTemplatesClick: () -> Unit,
    viewModel: TrackerViewModel
) {
    val isSaveActive = currentSave?.end == null

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Fixed top content
            Text(
                text = "Текущая активность",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ActivityCard(
                activity = currentActivity,
                viewModel = viewModel,
                expanded = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSaveActive) "Переключить активность" else "Сохранение завершено",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                IconButton(
                    onClick = onManageTemplatesClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Управление шаблонами",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity templates
            if (activityTemplates.isNotEmpty()) {
                val rows = activityTemplates.chunked(2)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rows) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { template ->
                                val isCurrentActivity = currentActivity?.name == template.name
                                val activityColor = parseColor(template.color) ?: Color.Transparent

                                OutlinedButton(
                                    onClick = { viewModel.onActivityTemplateSelected(template) },
                                    modifier = Modifier.weight(1f),
                                    enabled = currentSave != null && isSaveActive,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor =
                                            MaterialTheme.colorScheme.surfaceContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    border = if (isCurrentActivity)
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                                else
                                        null
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(activityColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = template.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                EmptyTemplatesMessage(modifier = Modifier.fillMaxWidth())
            }

            InfoMessages(
                currentSave = currentSave,
                isSaveActive = isSaveActive
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline section - always at the bottom
            TimelineSection(
                currentSave = currentSave,
                isSaveActive = isSaveActive,
                activityTemplates = activityTemplates
            )
        }
    }
}

@Composable
private fun TimelineSection(
    currentSave: com.grace.eva.domain.model.Save?,
    isSaveActive: Boolean,
    activityTemplates: List<com.grace.eva.domain.model.ActivityTemplate>
) {
    if (currentSave != null && currentSave.activities.isNotEmpty()) {
        val sortedActivities = currentSave.activities.sortedBy { it.begin }

        ActivitiesMapChart(
            activities = sortedActivities,
            isSaveCompleted = !isSaveActive,
            saveEnd = currentSave.end,
            getColorForActivity = { name ->
                val template = activityTemplates.find { it.name == name }
                parseColor(template?.color ?: "#2196F3") ?: Color(0xFF2196F3)
            },
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "Нет данных для отображения",
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyTemplatesMessage(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "Нет шаблонов активностей. Добавьте их в настройках.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoMessages(
    currentSave: com.grace.eva.domain.model.Save?,
    isSaveActive: Boolean
) {
    if (currentSave == null) {
        Text(
            text = "Выберите сохранение в настройках",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackerScreen() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }

    TrackerScreenContent(mockViewModel)
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackerScreen_NoSave() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.EMPTY))
    }

    TrackerScreenContent(mockViewModel)
}