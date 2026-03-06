package com.example.eva.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.eva.di.AppContainer
import com.example.eva.domain.model.Activities
import com.example.eva.domain.usecase.AddNoteToLastActivityUseCase
import com.example.eva.domain.usecase.DeleteLastActivityUseCase
import com.example.eva.domain.usecase.GetActivitiesUseCase
import com.example.eva.domain.usecase.NewActivityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

data class TrackerUiState (
    val activities: Activities = Activities(),
)

class TrackerViewModel(
    private val appContainer: AppContainer
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appContainer.getActivitiesUseCase().collect { activities ->
                _uiState.update {
                    it.copy(activities = activities)
                }
            }
        }
    }

    fun onNewActivity(name: String, note: String = "") {
        viewModelScope.launch {
            appContainer.newActivityUseCase(name, note)
        }
    }

    fun onDeleteLastActivity() {
        viewModelScope.launch {
            appContainer.deleteLastActivityUseCase()
        }
    }

    fun onAddNoteToLastActivityUseCase(note: String) {
        viewModelScope.launch {
            appContainer.addNoteToLastActivityUseCase(note)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val appContainer: AppContainer
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackerViewModel(
                appContainer
            ) as T
        }
    }
}