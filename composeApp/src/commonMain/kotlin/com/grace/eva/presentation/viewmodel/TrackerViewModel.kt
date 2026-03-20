package com.grace.eva.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.grace.eva.di.AppContainer
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.domain.model.Save
import com.grace.eva.utils.formatTime
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
    val activityTemplates: List<ActivityTemplate> = emptyList(),
    val isLoading: Boolean = false
)

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

class TrackerViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private val _currentActivity = MutableStateFlow<Activity?>(null)
    val currentActivity: StateFlow<Activity?> = _currentActivity.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appContainer.getAllSavesUseCase(),
                appContainer.getCurrentSaveUseCase(),
                appContainer.getActivityTemplatesUseCase()
            ) { allSaves: List<Save>, currentSave: Save?, templates: List<ActivityTemplate> ->
                TrackerUiState(
                    allSaves = allSaves,
                    currentSave = currentSave,
                    activityTemplates = templates
                )
            }.collect { state ->
                _uiState.value = state
                _currentActivity.value = state.currentSave?.activities?.lastOrNull()
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

    fun onUpdateSaveEndWithCallback(
        save: Save, newEnd: Instant?, onError: (String) -> Unit, onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val result = validateSaveEnd(save, newEnd)) {
                is ValidationResult.Success -> {
                    val updatedSave = save.copy(end = newEnd)
                    appContainer.updateSaveUseCase(updatedSave)
                    onSuccess()
                }
                is ValidationResult.Error -> {
                    onError(result.message)
                }
            }
        }
    }

    // Activity logic - единый метод обновления
    fun onUpdateActivity(
        activity: Activity,
        newName: String,
        newNote: String,
        newBegin: Instant,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val result = validateActivityBegin(activity, newBegin)) {
                is ValidationResult.Error -> {
                    onError(result.message)
                    return@launch
                }
                is ValidationResult.Success -> {
                    val updatedActivity = activity.copy(
                        name = newName,
                        note = newNote,
                        begin = newBegin
                    )
                    appContainer.updateActivityUseCase(updatedActivity)
                    onSuccess()
                }
            }
        }
    }

    fun onRemoveActivity(activity: Activity) {
        viewModelScope.launch {
            appContainer.removeActivityUseCase(activity)
        }
    }

    fun onCreateActivity(name: String) {
        viewModelScope.launch {
            appContainer.createActivityUseCase(name)
        }
    }

    fun onActivityTemplateSelected(template: ActivityTemplate) {
        if (_uiState.value.currentSave?.end == null) {
            onCreateActivity(template.name)
        }
    }

    // Template logic
    fun onAddActivityTemplate(name: String, color: String = "#2196F3") {
        viewModelScope.launch {
            appContainer.addActivityTemplateUseCase(name, color)
        }
    }

    fun onRemoveActivityTemplate(template: ActivityTemplate) {
        viewModelScope.launch {
            appContainer.removeActivityTemplateUseCase(template)
        }
    }

    fun onUpdateActivityTemplate(template: ActivityTemplate, newName: String, newColor: String) {
        viewModelScope.launch {
            val updatedTemplate = template.copy(name = newName, color = newColor)
            appContainer.updateActivityTemplateUseCase(updatedTemplate)
        }
    }

    fun onRenameActivityTemplate(template: ActivityTemplate, newName: String) {
        onUpdateActivityTemplate(template, newName, template.color)
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

    // Validation
    fun validateActivityBegin(activity: Activity, newBegin: Instant): ValidationResult {
        val currentSave = _uiState.value.currentSave ?: return ValidationResult.Error("No active save")
        val activities = currentSave.activities.sortedBy { it.begin }
        val index = activities.indexOfFirst { it.id == activity.id }

        if (index > 0) {
            val previousActivity = activities[index - 1]
            if (newBegin <= previousActivity.begin) {
                return ValidationResult.Error("Должно быть позже ${formatTime(previousActivity.begin)}")
            }
        }

        if (index < activities.lastIndex) {
            val nextActivity = activities[index + 1]
            if (newBegin >= nextActivity.begin) {
                return ValidationResult.Error("Должно быть раньше ${formatTime(nextActivity.begin)}")
            }
        }

        return ValidationResult.Success
    }

    fun validateSaveEnd(save: Save, newEnd: Instant?): ValidationResult {
        if (newEnd == null) return ValidationResult.Success

        val lastActivity = save.activities.maxByOrNull { it.begin }
        if (lastActivity != null && newEnd <= lastActivity.begin) {
            return ValidationResult.Error("Должно быть позже ${formatTime(lastActivity.begin)}")
        }

        return ValidationResult.Success
    }

    // Utility methods
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
        fun <T : ViewModel> create(modelClass: KClass<T>): T {
            return TrackerViewModel(appContainer) as T
        }

        override fun <T : ViewModel> create(
            modelClass: KClass<T>, extras: CreationExtras
        ): T {
            return TrackerViewModel(appContainer) as T
        }
    }
}