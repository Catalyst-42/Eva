package com.grace.eva.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.domain.model.Save
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.component.ActivityIcon
import com.grace.eva.presentation.component.chart.ActivitiesMapChart
import com.grace.eva.presentation.screen.floating.TemplateManagementScreen
import com.grace.eva.presentation.viewmodel.TrackerViewModel

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

    var showTemplatesScreen by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !showTemplatesScreen, enter = fadeIn(), exit = fadeOut()
        ) {
            MainTrackerContent(
                currentSave = currentSave,
                currentActivity = currentActivity,
                activityTemplates = activityTemplates,
                onManageTemplatesClick = { showTemplatesScreen = true },
                viewModel = viewModel
            )
        }

        AnimatedVisibility(
            visible = showTemplatesScreen, enter = slideInHorizontally(
                initialOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)
            ) + fadeIn(), exit = slideOutHorizontally(
                targetOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)
            ) + fadeOut()
        ) {
            TemplateManagementScreen(
                activityTemplates = activityTemplates,
                viewModel = viewModel,
                onClose = { showTemplatesScreen = false })
        }
    }
}

@Composable
private fun MainTrackerContent(
    currentSave: Save?, currentActivity: Activity?, activityTemplates: List<ActivityTemplate>,
    onManageTemplatesClick: () -> Unit, viewModel: TrackerViewModel
) {
    val isSaveActive = currentSave?.end == null

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Last activity
            Text(
                text = "Последняя активность",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        ActivityCard(
            activity = currentActivity, viewModel = viewModel, expanded = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Activity templates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Переключить активность",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            IconButton(
                onClick = onManageTemplatesClick, modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Управление шаблонами",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(158.dp)
        ) {
            if (activityTemplates.isNotEmpty()) {
                val rows = remember(activityTemplates) { activityTemplates.chunked(2) }
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { template ->
                                val isCurrentActivity = currentActivity?.name == template.name

                                OutlinedButton(
                                    onClick = { viewModel.onActivityTemplateSelected(template) },
                                    modifier = Modifier.weight(1f),
                                    enabled = currentSave != null && isSaveActive,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    border = if (isCurrentActivity || !isSaveActive) BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant
                                    )
                                    else null
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        ActivityIcon(template)
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
                EmptyTemplatesMessage(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeline
        TimelineSection(
            currentSave = currentSave, isSaveActive = isSaveActive, viewModel = viewModel
        )
    }
}

@Composable
private fun TimelineSection(
    currentSave: Save?, isSaveActive: Boolean, viewModel: TrackerViewModel
) {
    val activities = currentSave?.activities ?: emptyList()

    ActivitiesMapChart(
        activities = activities,
        isSaveCompleted = !isSaveActive,
        saveEnd = currentSave?.end,
        getColorForActivity = { name ->
            viewModel.getColorForActivity(name)
        },
        getActivityTemplateIsHidden = { name ->
            viewModel.getActivityTemplateIsHidden(name)
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EmptyTemplatesMessage(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "Начните с создания шаблона",
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrackerScreenPreview() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }

    TrackerScreenContent(mockViewModel)
}

@Preview(showBackground = true)
@Composable
fun TrackerScreenEmptyPreview() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.EMPTY))
    }

    TrackerScreenContent(mockViewModel)
}