package com.grace.eva.presentation.screen.floating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.presentation.component.TemplateCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.ui.theme.tracker.TemplateColors

@Composable
fun TemplateManagementScreen(
    activityTemplates: List<ActivityTemplate>, viewModel: TrackerViewModel, onClose: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Шаблоны активностей",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            IconButton(
                onClick = {
                    keyboardController?.hide()
                    onClose()
                }, modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // List of templates
            items(
                items = activityTemplates,
                key = { template -> template.id }
            ) {
                template ->
                TemplateCard(
                    template = template,
                    viewModel = viewModel
                )
            }

            // Button to add new
            item {
                Button(
                    onClick = {
                        val newIndex = activityTemplates.size
                        val newTemplateName = "Активность ${newIndex + 1}"
                        val newColor = TemplateColors.getColorForIndex(newIndex)

                        viewModel.onAddActivityTemplate(
                            name = newTemplateName, color = newColor
                        )
                    }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить шаблон")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TemplateManagementScreenPreview() {
    val mockViewModel = TrackerViewModel(
        appContainer = MockAppContainer(MockType.SIMPLE)
    )

    TemplateManagementScreen(
        activityTemplates = mockViewModel.uiState.value.activityTemplates,
        viewModel = mockViewModel,
        onClose = {}
    )
}

@Preview(showBackground = true)
@Composable
fun TemplateManagementScreenEmptyPreview() {
    val mockViewModel = TrackerViewModel(
        appContainer = MockAppContainer(MockType.EMPTY)
    )

    TemplateManagementScreen(
        activityTemplates = mockViewModel.uiState.value.activityTemplates,
        viewModel = mockViewModel,
        onClose = {}
    )
}