package com.grace.eva.data.repository

import android.content.Context
import android.util.Log
import com.grace.eva.domain.model.Save
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

private const val TRACKER_CONFIG_FILE = "tracker.json"
private const val SAVES_DIRECTORY = "saves"

@Serializable
data class TrackerMeta(
    val saves: List<String> = emptyList(),
    val currentSaveId: String? = null
)

class TrackerRepositoryImpl(
    private val context: Context
) : TrackerRepository {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // State
    private val _allSaves = MutableStateFlow<List<Save>>(emptyList())
    private val _currentSave = MutableStateFlow<Save?>(null)

    init {
        loadTrackerData()
    }

    private fun getTrackerMetaFile(): File {
        return File(context.filesDir, TRACKER_CONFIG_FILE)
    }

    private fun getSavesDirectory(): File {
        val dir = File(context.filesDir, SAVES_DIRECTORY)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getSaveFile(saveId: String): File {
        return File(getSavesDirectory(), "$saveId.json")
    }

    private fun loadTrackerData() {
        // Load meta file
        val metaFile = getTrackerMetaFile()
        val meta = if (metaFile.exists()) {
            try {
                json.decodeFromString<TrackerMeta>(metaFile.readText())
            } catch (e: Exception) {
                TrackerMeta()
            }
        } else {
            TrackerMeta()
        }

        // Load all saves
        val saves = meta.saves.mapNotNull { saveId ->
            val saveFile = getSaveFile(saveId)
            if (saveFile.exists()) {
                try {
                    json.decodeFromString<Save>(saveFile.readText())
                } catch (e: Exception) {
                    null
                }
            } else null
        }

        _allSaves.value = saves
        _currentSave.value = saves.find { it.id == meta.currentSaveId } ?: saves.firstOrNull()
    }

    private suspend fun saveTrackerMeta() {
        val meta = TrackerMeta(
            saves = _allSaves.value.map { it.id },
            currentSaveId = _currentSave.value?.id
        )
        val metaFile = getTrackerMetaFile()
        val jsonString = json.encodeToString(meta)
        metaFile.writeText(jsonString)
    }

    private suspend fun saveSaveToFile(save: Save) {
        val file = getSaveFile(save.id)
        val jsonString = json.encodeToString(save)
        file.writeText(jsonString)
    }

    private suspend fun deleteSaveFile(save: Save) {
        val file = getSaveFile(save.id)
        if (file.exists()) {
            file.delete()
        }
    }

    override suspend fun getAllSaves(): Flow<List<Save>> = _allSaves.asStateFlow()

    override suspend fun getCurrentSave(): Flow<Save?> = _currentSave.asStateFlow()

    override suspend fun setCurrentSave(save: Save) {
        _currentSave.value = save
        saveTrackerMeta()
    }

    override suspend fun createSave(name: String): Save {
        val newSave = Save(name = name)
        _allSaves.value += newSave
        _currentSave.value = newSave

        saveSaveToFile(newSave)
        saveTrackerMeta()

        return newSave
    }

    override suspend fun deleteSave(save: Save) {
        _allSaves.value = _allSaves.value.filter { it.id != save.id }
        if (_currentSave.value?.id == save.id) {
            _currentSave.value = _allSaves.value.firstOrNull()
        }

        deleteSaveFile(save)
        saveTrackerMeta()
    }

    override suspend fun updateSave(save: Save) {
        _allSaves.value = _allSaves.value.map { if (it.id == save.id) save else it }
        if (_currentSave.value?.id == save.id) {
            _currentSave.value = save
        }

        saveSaveToFile(save)
        saveTrackerMeta()
    }

    override suspend fun addActivity(name: String) {
        val current = _currentSave.value ?: return
        val newActivity = Activity(name = name)
        val updatedSave = current.copy(
            activities = (current.activities + newActivity).toMutableList()
        )
        updateSave(updatedSave)
    }

    override suspend fun removeActivity(activity: Activity) {
        val current = _currentSave.value ?: return
        val updatedSave = current.copy(
            activities = current.activities.filter { it.id != activity.id }.toMutableList()
        )
        updateSave(updatedSave)
    }

    override suspend fun updateActivity(activity: Activity) {
        val current = _currentSave.value ?: return
        val updatedSave = current.copy(
            activities = current.activities.map { if (it.id == activity.id) activity else it }.toMutableList()
        )
        updateSave(updatedSave)
    }

    override suspend fun exportSave(save: Save) {
        // TODO: Implement share functionality
    }

    override suspend fun importSave() {
        // TODO: make loadable from input file
    }
}