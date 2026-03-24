package com.grace.eva.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.util.parseColor

@Composable
fun TemplateCard(
    template: ActivityTemplate,
    viewModel: TrackerViewModel,
    expanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(expanded) }
    var editedName by remember(template.id, template.name) {
        mutableStateOf(template.name)
    }
    var editedColor by remember(template.id, template.color) {
        mutableStateOf(template.color)
    }
    var isHidden by remember(template.id, template.color) {
        mutableStateOf(template.isHidden)
    }
    var formatError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium,
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActivityIcon(template)

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Название шаблона") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = editedName.isBlank()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedColor,
                    onValueChange = {
                        editedColor = it
                    },
                    label = { Text("Цвет") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = formatError || validationError != null,
                    supportingText = if (formatError || validationError != null) {
                        {
                            when {
                                formatError -> Text("Используйте формат #RRGGBB")
                                else -> Text(validationError!!)
                            }
                        }
                    } else null
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                        onClick = {
                            isHidden = !isHidden
                        }
                    )
                ) {
                    Checkbox(
                        checked = isHidden,
                        onCheckedChange = {
                            isHidden = it
                        }
                    )

                    Text("Скрывать на карте недели")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.onRemoveActivityTemplate(template)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Удалить")
                    }

                    Button(
                        onClick = {
                            // Validate color format
                            val color = parseColor(editedColor)
                            if (color == null) {
                                formatError = true
                                return@Button
                            }
                            formatError = false
                            validationError = null

                            // Validate name
                            if (editedName.isBlank()) {
                                validationError = "Название не может быть пустым"
                                return@Button
                            }

                            // Use unified update method
                            viewModel.onUpdateActivityTemplate(
                                template = template,
                                newName = editedName,
                                newColor = editedColor,
                                newIsHidden = isHidden,
                                onSuccess = {
                                    validationError = null
                                }
                            )
                        },
                        modifier = Modifier.weight(2f),
                        enabled = editedName.isNotBlank()
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTemplateCardClosed() {
    val template = ActivityTemplate()
    val mockViewModel = TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))

    TemplateCard(
        template = template,
        viewModel = mockViewModel,
        expanded = false
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTemplateCardExpanded() {
    val template = ActivityTemplate(
        name = "Eva is Experiment version Android",
        color = "Broken one"
    )
    val mockViewModel = TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))

    TemplateCard(
        template = template,
        viewModel = mockViewModel,
        expanded = true
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTemplateCardHidden() {
    val template = ActivityTemplate(
        name = "Hidden one",
        color = "#FF00FF",
        isHidden = true
    )
    val mockViewModel = TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))

    TemplateCard(
        template = template,
        viewModel = mockViewModel,
        expanded = true
    )
}
