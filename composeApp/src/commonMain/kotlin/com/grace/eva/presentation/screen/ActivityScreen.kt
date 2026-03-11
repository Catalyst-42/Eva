package com.grace.eva.presentation.screen

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grace.eva.di.AppContainer
import com.grace.eva.presentation.component.ActivityCard
import com.grace.eva.presentation.viewmodel.TrackerViewModel
import kotlinx.coroutines.delay
import kotlin.time.Clock

@Composable
fun ActivityScreen(
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

    var currentTime by remember { mutableStateOf(Clock.System.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            // Recompose timer
            currentTime = Clock.System.now()
            delay(1000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = activities.reversed(),
            key = { activity -> "${activity.begin}-${activity.name}" }
        ) { activity ->
            ActivityCard(
                activity = activity,
                now = currentTime
            )
        }
    }
}
