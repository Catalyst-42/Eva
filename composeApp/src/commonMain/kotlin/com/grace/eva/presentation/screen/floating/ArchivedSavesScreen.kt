package com.grace.eva.presentation.screen.floating

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.domain.model.Save
import com.grace.eva.presentation.component.SaveCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val ArchivedSavesListBottomPadding = 320.dp

@Composable
fun ArchivedSavesScreen(
    saves: List<Save>, viewModel: TrackerViewModel, onClose: () -> Unit,
    onMessage: (String) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.onMoveArchivedSave(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Архив сохранений",
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

        if (saves.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Text(
                    text = "В архиве пока нет сохранений.", modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = ArchivedSavesListBottomPadding)
            ) {
                items(
                    items = saves, key = { save -> save.id }) { save ->
                    ReorderableItem(
                        reorderableLazyListState,
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
                            onMessage = onMessage)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchivedSavesScreenPreview() {
    val mockViewModel = TrackerViewModel(
        appContainer = MockAppContainer(MockType.LARGE)
    )

    ArchivedSavesScreen(
        saves = listOf(
            mockViewModel.uiState.value.currentSave?.copy(isArchived = true)
                ?: Save(isArchived = true)
        ), viewModel = mockViewModel, onClose = {})
}