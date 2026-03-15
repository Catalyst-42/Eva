package com.grace.eva.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class MockTrackerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        TrackerUiState(
            activities = Activities(
                name = "Mock Save",
                activities = mutableListOf(
                    Activity(
                        name = "Первая активность",
                        note = "Тестовое описание",
                        begin = Clock.System.now() - 2.hours,
                        end = Clock.System.now() - 1.hours - 30.minutes
                    ),
                    Activity(
                        name = "Вторая активность",
                        note = "",
                        begin = Clock.System.now() - 1.hours - 30.minutes,
                        end = Clock.System.now() - 30.minutes
                    ),
                    Activity(
                        name = "Текущая активность",
                        note = "В процессе",
                        begin = Clock.System.now() - 30.minutes,
                        end = null
                    )
                )
            )
        )
    )
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    fun onNewActivity(name: String, note: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val newActivity = Activity(
                    name = name,
                    note = note,
                    begin = Clock.System.now()
                )
                val newList = state.activities.activities.toMutableList()
                newList.add(newActivity)
                state.copy(
                    activities = state.activities.copy(
                        activities = newList
                    )
                )
            }
        }
    }

    fun onDeleteActivity(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { state ->
                val newList = state.activities.activities
                    .filter { it.id != activity.id }
                    .toMutableList()
                state.copy(
                    activities = state.activities.copy(
                        activities = newList
                    )
                )
            }
        }
    }

    fun onAddNoteToLastActivityUseCase(note: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val activities = state.activities.activities.toMutableList()
                if (activities.isNotEmpty()) {
                    val lastActivity = activities.last()
                    val updatedActivity = lastActivity.copy(note = note)
                    activities[activities.size - 1] = updatedActivity
                }
                state.copy(
                    activities = state.activities.copy(
                        activities = activities
                    )
                )
            }
        }
    }

    fun onSaveActivities() {
        // Just log or do nothing
    }

    fun onUpdateActivity(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { state ->
                val newList = state.activities.activities
                    .map { if (it.id == activity.id) activity else it }
                    .toMutableList()
                state.copy(
                    activities = state.activities.copy(
                        activities = newList
                    )
                )
            }
        }
    }

    fun onActivitiesExport(activities: Activities) {
        // Just log or do nothing
    }
}

// For preview usage with different data
object MockData {
    val emptyActivities = Activities(
        name = "Пустое сохранение",
        activities = mutableListOf()
    )

    val singleActivity = Activities(
        name = "Одна активность",
        activities = mutableListOf(
            Activity(
                name = "Единственная",
                note = "Тест",
                begin = Clock.System.now() - 1.hours,
                end = null
            )
        )
    )

    val completedActivities = Activities(
        name = "Завершенные",
        activities = mutableListOf(
            Activity(
                name = "Первая",
                note = "",
                begin = Clock.System.now() - 3.hours,
                end = Clock.System.now() - 2.hours
            ),
            Activity(
                name = "Вторая",
                note = "Готово",
                begin = Clock.System.now() - 2.hours,
                end = Clock.System.now() - 1.hours
            ),
            Activity(
                name = "Третья",
                note = "Завершено",
                begin = Clock.System.now() - 1.hours,
                end = Clock.System.now()
            )
        )
    )
}