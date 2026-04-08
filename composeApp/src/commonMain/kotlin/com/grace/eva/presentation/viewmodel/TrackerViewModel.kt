package com.grace.eva.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.grace.eva.di.AppContainer
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.domain.model.ConnectionCheckResult
import com.grace.eva.domain.model.Save
import com.grace.eva.domain.model.Tracker
import com.grace.eva.ui.theme.tracker.TemplateColors
import com.grace.eva.util.formatTime
import com.grace.eva.util.parseColor
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
    val tracker: Tracker = Tracker(),
    val allSaves: List<Save> = emptyList(),
    val currentSave: Save? = null,
    val activityTemplates: List<ActivityTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val isCheckingConnection: Boolean = false,
    val lastConnectionCheck: ConnectionCheckResult? = null
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
                appContainer.trackerRepository.getTracker(),
                appContainer.getAllSavesUseCase(),
                appContainer.getCurrentSaveUseCase(),
                appContainer.getActivityTemplatesUseCase()
            ) { tracker: Tracker, allSaves: List<Save>, currentSave: Save?, templates: List<ActivityTemplate> ->
                TrackerUiState(
                    tracker = tracker,
                    allSaves = allSaves,
                    currentSave = currentSave,
                    activityTemplates = templates,
                    isLoading = _uiState.value.isLoading,
                    isCheckingConnection = _uiState.value.isCheckingConnection,
                    lastConnectionCheck = _uiState.value.lastConnectionCheck
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

    fun onExportSave(save: Save) {
        viewModelScope.launch {
            appContainer.exportSaveUseCase(save)
        }
    }

    fun onSetSaveArchived(save: Save, isArchived: Boolean) {
        viewModelScope.launch {
            appContainer.updateSaveUseCase(save.copy(isArchived = isArchived))
        }
    }

    fun onImportSave() {
        viewModelScope.launch {
            appContainer.importSaveUseCase()
        }
    }

    fun onMoveActiveSave(fromIndex: Int, toIndex: Int) {
        val allSaves = _uiState.value.allSaves
        val activeSaves = allSaves.filterNot { it.isArchived }
        val reorderedActiveSaves = activeSaves.move(fromIndex, toIndex) ?: return

        viewModelScope.launch {
            appContainer.trackerRepository.reorderSaves(
                mergeSaveSubset(
                    allSaves = allSaves,
                    reorderedSubset = reorderedActiveSaves,
                    isTarget = { !it.isArchived }
                )
            )
        }
    }

    fun onMoveArchivedSave(fromIndex: Int, toIndex: Int) {
        val allSaves = _uiState.value.allSaves
        val archivedSaves = allSaves.filter { it.isArchived }
        val reorderedArchivedSaves = archivedSaves.move(fromIndex, toIndex) ?: return

        viewModelScope.launch {
            appContainer.trackerRepository.reorderSaves(
                mergeSaveSubset(
                    allSaves = allSaves,
                    reorderedSubset = reorderedArchivedSaves,
                    isTarget = { it.isArchived }
                )
            )
        }
    }

    fun onSyncSave(
        save: Save,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appContainer.trackerRepository.syncSaveWithServer(save)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Не удалось синхронизировать сохранение")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onUpdateRemoteServerUrl(url: String) {
        viewModelScope.launch {
            appContainer.trackerRepository.updateRemoteServerUrl(url)
        }
    }

    fun onTestRemoteServerConnection(url: String? = null) {
        runConnectionCheck {
            if (url != null) {
                appContainer.trackerRepository.updateRemoteServerUrl(url)
            }
            appContainer.trackerRepository.testRemoteServerConnection()
        }
    }

    // Unified update method with callback
    fun onUpdateSave(
        save: Save, newName: String? = null, newEnd: Instant? = null, onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Validate end time if provided
            if (newEnd != null) {
                when (val result = validateSaveEnd(save, newEnd)) {
                    is ValidationResult.Error -> {
                        onError(result.message)
                        return@launch
                    }

                    is ValidationResult.Success -> {
                        // Continue validation
                    }
                }
            }

            // Build updated save
            var updatedSave = save
            if (newName != null && newName != save.name) {
                updatedSave = updatedSave.copy(name = newName)
            }
            if (newEnd != null && newEnd != save.end) {
                updatedSave = updatedSave.copy(end = newEnd)
            } else if (newEnd == null && save.end != null) {
                updatedSave = updatedSave.copy(end = null)
            }

            // Only update if something changed
            if (updatedSave != save) {
                appContainer.updateSaveUseCase(updatedSave)
            }

            onSuccess()
        }
    }
    // Activity logic
    fun onUpdateActivity(
        activity: Activity, newName: String, newNote: String, newBegin: Instant,
        onError: (String) -> Unit, onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val result = validateActivityBegin(activity, newBegin)) {
                is ValidationResult.Error -> {
                    onError(result.message)
                    return@launch
                }

                is ValidationResult.Success -> {
                    val updatedActivity = activity.copy(
                        name = newName, note = newNote, begin = newBegin
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

    fun onUpdateActivityTemplate(
        template: ActivityTemplate, newName: String, newColor: String, newIsHidden: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Add validation if needed
            val updatedTemplate = template.copy(
                name = newName, color = newColor, isHidden = newIsHidden
            )
            appContainer.updateActivityTemplateUseCase(updatedTemplate)
            onSuccess()
        }
    }

    fun onMoveActivityTemplate(fromIndex: Int, toIndex: Int) {
        val reorderedTemplates = _uiState.value.activityTemplates.move(fromIndex, toIndex) ?: return

        viewModelScope.launch {
            appContainer.trackerRepository.reorderActivityTemplates(reorderedTemplates)
        }
    }

    fun getTemplateForActivity(activityName: String): ActivityTemplate? {
        val templates = _uiState.value.activityTemplates
        val template = templates.find { it.name == activityName }

        return template
    }

    fun getColorForActivity(activityName: String): Color {
        val template = getTemplateForActivity(activityName)
        val hexColor = template?.color ?: TemplateColors.getColorForIndex(0)

        return parseColor(hexColor) ?: TemplateColors.getDefaultColor()
    }

    fun getActivityTemplateIsHidden(activityName: String): Boolean {
        val templates = _uiState.value.activityTemplates
        val template = templates.find { it.name == activityName }
        val isHidden = template?.isHidden ?: false

        return isHidden
    }

    // Validation
    fun validateActivityBegin(activity: Activity, newBegin: Instant): ValidationResult {
        val currentSave =
            _uiState.value.currentSave ?: return ValidationResult.Error("No active save")
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

    private fun runConnectionCheck(
        block: suspend () -> ConnectionCheckResult
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingConnection = true, lastConnectionCheck = null) }
            val result = block()
            _uiState.update { it.copy(isCheckingConnection = false, lastConnectionCheck = result) }
        }
    }

    private fun mergeSaveSubset(
        allSaves: List<Save>,
        reorderedSubset: List<Save>,
        isTarget: (Save) -> Boolean
    ): List<Save> {
        val reorderedIterator = reorderedSubset.iterator()
        return allSaves.map { save ->
            if (isTarget(save)) reorderedIterator.next() else save
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val appContainer: AppContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNUSED") // Used in iOS
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

private fun <T> List<T>.move(fromIndex: Int, toIndex: Int): List<T>? {
    if (isEmpty()) return null

    val normalizedFrom = fromIndex.coerceIn(indices)
    if (normalizedFrom == toIndex) return null

    val mutableList = toMutableList()
    val item = mutableList.removeAt(normalizedFrom)
    val adjustedTo = toIndex.coerceIn(0, mutableList.size)
    mutableList.add(adjustedTo, item)
    return mutableList
}
