package com.grace.eva.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.grace.eva.di.AppContainer
import com.grace.eva.domain.model.Save
import com.grace.eva.domain.model.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Instant

data class TrackerUiState(
    val allSaves: List<Save> = emptyList(),
    val currentSave: Save? = null,
    val isLoading: Boolean = false
)

class TrackerViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appContainer.getAllSavesUseCase(),
                appContainer.getCurrentSaveUseCase()
            ) { allSaves, currentSave ->
                TrackerUiState(
                    allSaves = allSaves,
                    currentSave = currentSave
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    // Save logic
    fun onCreateSave(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            appContainer.createSaveUseCase(name)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onDeleteSave(save: Save) {
        viewModelScope.launch {
            appContainer.deleteSaveUseCase(save)
        }
    }

    fun onSetCurrentSave(save: Save) {
        viewModelScope.launch {
            appContainer.setCurrentSaveUseCase(save)
        }
    }

    fun onRenameSave(save: Save, newName: String) {
        viewModelScope.launch {
            val updatedSave = save.copy(name = newName)
            appContainer.updateSaveUseCase(updatedSave)
        }
    }

    fun onCompleteSave(save: Save) {
        viewModelScope.launch {
            val updatedSave = save.copy(end = Clock.System.now())
            appContainer.updateSaveUseCase(updatedSave)
        }
    }

    fun onContinueSave(save: Save) {
        viewModelScope.launch {
            val updatedSave = save.copy(end = null)
            appContainer.updateSaveUseCase(updatedSave)
        }
    }

    fun onUpdateSaveEnd(save: Save, end: Instant?) {
        val currentSave = _uiState.value.currentSave ?: return
        viewModelScope.launch {
            val updatedSave = currentSave.copy(end = end)
            appContainer.updateSaveUseCase(updatedSave)
        }
    }

    // Activity logic
    fun onCreateActivity(name: String) {
        viewModelScope.launch {
            appContainer.createActivityUseCase(name)
        }
    }

    fun onRemoveActivity(activity: Activity) {
        viewModelScope.launch {
            appContainer.removeActivityUseCase(activity)
        }
    }

    fun onUpdateActivity(activity: Activity) {
        viewModelScope.launch {
            appContainer.updateActivityUseCase(activity)
        }
    }

    fun onRenameActivity(activity: Activity, newName: String) {
        viewModelScope.launch {
            val updatedActivity = activity.copy(name = newName)
            appContainer.updateActivityUseCase(updatedActivity)
        }
    }

    fun onUpdateActivityNote(activity: Activity, newNote: String) {
        viewModelScope.launch {
            val updatedActivity = activity.copy(note = newNote)
            appContainer.updateActivityUseCase(updatedActivity)
        }
    }

    fun onUpdateActivityBegin(activity: Activity, newBegin: Instant) {
        viewModelScope.launch {
            val updatedActivity = activity.copy(begin = newBegin)
            appContainer.updateActivityUseCase(updatedActivity)
        }
    }

    // Sync logic
    fun onExportSave(save: Save) {
        viewModelScope.launch {
            appContainer.exportSaveUseCase(save)
        }
    }

    fun onImportSave() {
        viewModelScope.launch {
            appContainer.importSaveUseCase()
        }
    }

    // Utility methods
    fun getSaveById(id: String): Save? {
        return _uiState.value.allSaves.find { it.id == id }
    }

    fun getActivityEndTime(activity: Activity): Instant? {
        val currentSave = _uiState.value.currentSave ?: return null
        val activities = currentSave.activities.sortedBy { it.begin }
        val index = activities.indexOfFirst { it.id == activity.id }

        return when {
            index < activities.lastIndex -> activities[index + 1].begin
            currentSave.end != null -> currentSave.end
            else -> null
        }
    }

    fun isCurrentSaveCompleted(): Boolean {
        return _uiState.value.currentSave?.end != null
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val appContainer: AppContainer
    ) : ViewModelProvider.Factory {
        // This one is for KMM with KClass
        fun <T : ViewModel> create(modelClass: KClass<T>): T {
            return TrackerViewModel(appContainer) as T
        }

        // Sometimes also need this one with key
        override fun <T : ViewModel> create(
            modelClass: KClass<T>,
            extras: CreationExtras
        ): T {
            return TrackerViewModel(appContainer) as T
        }
    }
}