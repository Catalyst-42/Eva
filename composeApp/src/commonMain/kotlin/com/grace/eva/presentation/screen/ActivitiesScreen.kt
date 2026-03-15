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
import androidx.compose.material.icons.filled.Search
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
import com.grace.eva.domain.model.Activity
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import kotlinx.coroutines.launch

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
    val activities = state.activities.activities

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    val focusManager = LocalFocusManager.current

    val filteredActivities = remember(searchQuery, activities) {
        if (searchQuery.isBlank()) {
            activities
        } else {
            activities.filter { activity ->
                activity.name.contains(searchQuery, ignoreCase = true) ||
                        activity.note.contains(searchQuery, ignoreCase = true)
            }
        }
    }.sortedByDescending { it.begin } // Sort by begin time descending (most recent first)

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
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                    placeholder = { Text("Поиск") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
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
                    IconButton(onClick = { searchQuery = "" }) {
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
            if (filteredActivities.isEmpty()) {
                item {
                    EmptyActivitiesCard()
                }
            } else {
                items(
                    items = filteredActivities,
                    key = { activity -> "${activity.id}-${activity.begin}" }
                ) { activity ->
                    // Find the next activity (the one that started after this one)
                    val currentIndex = filteredActivities.indexOf(activity)
                    val nextActivityBegin = if (currentIndex > 0) {
                        // Since we're sorted descending, the next activity in chronological order
                        // is actually the previous item in the list
                        filteredActivities[currentIndex - 1].begin
                    } else {
                        // This is the most recent activity (first in the list)
                        null
                    }

                    ActivityCard(
                        activity = activity,
                        nextActivityBegin = nextActivityBegin,
                        onActivityChange = { activity -> viewModel.onUpdateActivity(activity) },
                        onActivityDelete = { activity -> viewModel.onDeleteActivity(activity) }
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
fun PreviewEmptyActivitiesCard_NoActivities() {
    EmptyActivitiesCard()
}

@Preview(showBackground = true)
@Composable
fun PreviewActivitiesScreen() {
    val mockViewModel = remember {
        TrackerViewModel(appContainer = MockAppContainer(MockType.LARGE))
    }

    ActivityScreenContent(mockViewModel)
}