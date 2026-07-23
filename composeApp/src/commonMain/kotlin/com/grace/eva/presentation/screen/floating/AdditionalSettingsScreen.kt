package com.grace.eva.presentation.screen.floating

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.presentation.viewmodel.TrackerViewModel

private val BottomPadding = 320.dp

@Composable
fun AdditionalSettingsScreen(
    viewModel: TrackerViewModel,
    onClose: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val state by viewModel.uiState.collectAsState()
    var remoteServerUrl by rememberSaveable { mutableStateOf(state.tracker.remoteServerUrl) }
    var weeklyTimelineHeightInput by rememberSaveable { mutableStateOf(state.tracker.weeklyTimelineHeight.toString()) }
    var formatError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.tracker.remoteServerUrl) {
        remoteServerUrl = state.tracker.remoteServerUrl
    }

    LaunchedEffect(state.tracker.weeklyTimelineHeight) {
        weeklyTimelineHeightInput = state.tracker.weeklyTimelineHeight.toString()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = BottomPadding)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Дополнительные настройки",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                IconButton(
                    onClick = {
                        onClose()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Подключение к серверу",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = remoteServerUrl,
                        onValueChange = { remoteServerUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Адрес удалённого сервера") }
                    )

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.onUpdateRemoteServerUrl(remoteServerUrl)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сохранить адрес сервера")
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.onTestRemoteServerConnection(remoteServerUrl)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isCheckingConnection
                    ) {
                        Text(if (state.isCheckingConnection) "Проверка..." else "Проверить сервер")
                    }
                }
            }
        }

        state.lastConnectionCheck?.let { result ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.isSuccess) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = result.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (result.isSuccess) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                        Text(
                            text = result.message,
                            color = if (result.isSuccess) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Карта недели",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = weeklyTimelineHeightInput,
                        onValueChange = {
                            weeklyTimelineHeightInput = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Высота графика") },
                        isError = formatError || validationError != null,
                        supportingText = if (formatError || validationError != null) {
                            {
                                when {
                                    formatError -> Text("Неверный формат: введите число")
                                    else -> Text(validationError!!)
                                }
                            }
                        } else null
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                keyboardController?.hide()
                                weeklyTimelineHeightInput = "220"
                                formatError = false
                                validationError = null
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
                            Text(
                                text = "Сбросить",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                val newValue = weeklyTimelineHeightInput.toIntOrNull()
                                if (newValue == null) {
                                    formatError = true
                                    return@Button
                                }

                                if (newValue !in 100..1000) {
                                    validationError = "Значение должно быть от 100 до 1000"
                                    return@Button
                                }

                                formatError = false
                                validationError = null
                                viewModel.onUpdateWeeklyTimelineHeight(newValue)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Сохранить"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdditionalSettingsScreenPreview() {
    val mockViewModel = TrackerViewModel(
        appContainer = MockAppContainer(MockType.SIMPLE)
    )

    AdditionalSettingsScreen(
        viewModel = mockViewModel,
        onClose = {}
    )
}