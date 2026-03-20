package com.grace.eva.presentation.screen

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
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.screen.floating.TemplateManagementScreen
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.utils.parseColor

data class TemplateManagementState(
    val isManaging: Boolean = false
)

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

    var managementState by remember { mutableStateOf(TemplateManagementState()) }

    if (managementState.isManaging) {
        TemplateManagementScreen(
            activityTemplates = activityTemplates,
            viewModel = viewModel,
            onClose = { managementState = managementState.copy(isManaging = false) }
        )
    } else {
        MainTrackerContent(
            currentSave = currentSave,
            currentActivity = currentActivity,
            activityTemplates = activityTemplates,
            onManageTemplatesClick = {
                managementState = managementState.copy(isManaging = true)
            },
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
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
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Управление шаблонами",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activityTemplates.isEmpty()) {
            EmptyTemplatesMessage()
        } else {
            ActivityTemplateButtons(
                activityTemplates = activityTemplates,
                isSaveActive = isSaveActive,
                currentSave = currentSave,
                viewModel = viewModel
            )
        }

        InfoMessages(
            currentSave = currentSave,
            isSaveActive = isSaveActive
        )
    }
}

@Composable
private fun ActivityTemplateButtons(
    activityTemplates: List<com.grace.eva.domain.model.ActivityTemplate>,
    isSaveActive: Boolean,
    currentSave: com.grace.eva.domain.model.Save?,
    viewModel: TrackerViewModel
) {
    activityTemplates.chunked(2).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { template ->
                OutlinedButton(
                    onClick = { viewModel.onActivityTemplateSelected(template) },
                    modifier = Modifier.weight(1f),
                    enabled = currentSave != null && isSaveActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
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
                                .background(
                                    parseColor(template.color) ?: Color.Transparent
                                )
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
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun EmptyTemplatesMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
    } else if (!isSaveActive) {
        Text(
            text = "Чтобы переключать этапы, продолжите сохранение в настройках",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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