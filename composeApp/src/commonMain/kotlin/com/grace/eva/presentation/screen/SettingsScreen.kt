package com.grace.eva.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.presentation.component.SaveCard
import com.grace.eva.presentation.screen.floating.ArchivedSavesScreen
import com.grace.eva.presentation.screen.floating.RemoteConnectionsScreen
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val SettingsSavesListBottomPadding = 320.dp

@Composable
fun SettingsScreen(
    appContainer: AppContainer
) {
    val viewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModel.Factory(appContainer)
    )

    SettingsScreenContent(
        viewModel = viewModel
    )
}

@Composable
fun SettingsScreenContent(
    viewModel: TrackerViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val allSaves = state.allSaves
    val activeSaves = remember(allSaves) { allSaves.filterNot { it.isArchived } }
    val archivedSaves = remember(allSaves) { allSaves.filter { it.isArchived } }
    var showRemoteConnectionsScreen by rememberSaveable { mutableStateOf(false) }
    var showArchivedSavesScreen by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.onMoveActiveSave(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !showRemoteConnectionsScreen && !showArchivedSavesScreen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Настройки сохранений",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showArchivedSavesScreen = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "Показать архив сохранений",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showRemoteConnectionsScreen = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsEthernet,
                                contentDescription = "Настроить удалённые подключения",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = SettingsSavesListBottomPadding)
                ) {
                    if (activeSaves.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Text(
                                    text = if (archivedSaves.isEmpty()) {
                                        "Нет сохранений. Создайте новое."
                                    } else {
                                        "Все сохранения сейчас в архиве."
                                    }, modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(
                            items = activeSaves, key = { save -> save.id }) { save ->
                            ReorderableItem(
                                state = reorderableLazyListState,
                                key = save.id,
                                animateItemModifier = Modifier.animateItem(
                                    placementSpec = spring(
                                        stiffness = Spring.StiffnessHigh,
                                        visibilityThreshold = IntOffset.VisibilityThreshold
                                    )
                                )
                            ) {
                                val interactionSource = remember { MutableInteractionSource() }

                                SaveCard(
                                    save = save,
                                    viewModel = viewModel,
                                    modifier = Modifier.longPressDraggableHandle(
                                        interactionSource = interactionSource,
                                        onDragStarted = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        },
                                        onDragStopped = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                        }),
                                    interactionSource = interactionSource,
                                    onMessage = { message ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    })
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.onCreateSave("Новое сохранение ${allSaves.size + 1}")
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Создать новое сохранение")
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.onImportSave()
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Импортировать файл сохранения")
                        }
                    }

                }
            }
        }

        AnimatedVisibility(
            visible = showRemoteConnectionsScreen, enter = slideInHorizontally(
                initialOffsetX = { it }, animationSpec = tween(300)
            ) + fadeIn(), exit = slideOutHorizontally(
                targetOffsetX = { it }, animationSpec = tween(300)
            ) + fadeOut()
        ) {
            RemoteConnectionsScreen(
                viewModel = viewModel, onClose = { showRemoteConnectionsScreen = false })
        }

        AnimatedVisibility(
            visible = showArchivedSavesScreen, enter = slideInHorizontally(
                initialOffsetX = { it }, animationSpec = tween(300)
            ) + fadeIn(), exit = slideOutHorizontally(
                targetOffsetX = { it }, animationSpec = tween(300)
            ) + fadeOut()
        ) {
            ArchivedSavesScreen(
                saves = archivedSaves,
                viewModel = viewModel,
                onClose = { showArchivedSavesScreen = false },
                onMessage = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                })
        }

        SnackbarHost(
            hostState = snackbarHostState, snackbar = { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.LARGE))
    }

    SettingsScreenContent(
        viewModel = mockViewModel
    )
}