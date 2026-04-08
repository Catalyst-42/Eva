package com.grace.eva.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grace.eva.di.AppContainer
import com.grace.eva.di.MockAppContainer
import com.grace.eva.di.MockType
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import com.grace.eva.util.formatTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ActivitiesScreen(
    appContainer: AppContainer
) {
    val viewModel: TrackerViewModel = viewModel(
        factory = TrackerViewModel.Factory(appContainer)
    )

    ActivityScreenContent(viewModel)
}

@Composable
fun ActivityScreenContent(viewModel: TrackerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val currentSaveId = state.currentSave?.id

    // Safe handling of currentSave
    val activities = state.currentSave?.activities ?: emptyList()

    var searchQuery by rememberSaveable(currentSaveId) { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    val focusManager = LocalFocusManager.current

    var debouncedSearchQuery by remember(currentSaveId) { mutableStateOf(searchQuery) }

    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedSearchQuery = searchQuery
    }

    val filteredActivities by produceState(
        initialValue = activities.sortedByDescending { it.begin },
        key1 = activities,
        key2 = debouncedSearchQuery
    ) {
        value = withContext(Dispatchers.Default) {
            val filtered = if (debouncedSearchQuery.isBlank()) {
                activities
            } else {
                activities.filter { activity ->
                    activity.name.contains(debouncedSearchQuery, ignoreCase = true) ||
                        activity.note.contains(debouncedSearchQuery, ignoreCase = true) ||
                        formatTime(activity.begin, "dd.mm.yyyy").contains(debouncedSearchQuery)
                }
            }
            filtered.sortedByDescending { it.begin }
        }
    }

    fun scrollToTop() {
        scope.launch {
            scrollState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Show warning if no save is selected
        if (state.currentSave == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Нет активного сохранения. Выберите сохранение в списке.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Text(
            text = "Последние активности",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Floating search card styled like inactive card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Поиск",
                            style = MaterialTheme.typography.titleMedium
                        )
                      },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search,
                        autoCorrectEnabled = false
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            scope.launch {
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            focusManager.clearFocus()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Очистить поиск"
                        )
                    }
                }
            }
        }

        // Title and results count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "История",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable {
                    scrollToTop()
                }
            )

            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "Найдено: ${filteredActivities.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Activities list
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            if (state.currentSave == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Выберите сохранение для просмотра активностей",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (filteredActivities.isEmpty()) {
                item {
                    EmptyActivitiesCard()
                }
            } else {
                items(
                    items = filteredActivities,
                    key = { activity -> activity.id }
                ) { activity ->
                    ActivityCard(
                        activity = activity,
                        viewModel = viewModel,
                        expanded = false
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyActivitiesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Нет активностей",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivitiesScreenPreview() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.SIMPLE))
    }

    ActivityScreenContent(mockViewModel)
}

@Preview(showBackground = true)
@Composable
fun ActivitiesScreenEmptyPreview() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.EMPTY))
    }

    ActivityScreenContent(mockViewModel)
}
