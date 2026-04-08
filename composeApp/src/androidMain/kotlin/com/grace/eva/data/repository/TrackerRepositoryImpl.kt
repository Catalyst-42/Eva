package com.grace.eva.data.repository

import android.content.Context
import android.util.Log
import androidx.core.content.FileProvider
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.domain.model.Save
import com.grace.eva.domain.model.Tracker
import com.grace.eva.domain.repository.TrackerRepository
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TRACKER_CONFIG_FILE = "tracker.json"
private const val SAVES_DIRECTORY = "saves"

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

    private fun getTrackerConfigFile(): File {
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
        // Load config file
        val configFile = getTrackerConfigFile()
        val config = if (configFile.exists()) {
            try {
                json.decodeFromString<Tracker>(configFile.readText())
            } catch (_: Exception) {
                Tracker()
            }
        } else {
            Tracker()
        }

        // Load all saves
        val saves = config.saves.mapNotNull { saveId ->
            val saveFile = getSaveFile(saveId)
            if (saveFile.exists()) {
                try {
                    json.decodeFromString<Save>(saveFile.readText())
                } catch (_: Exception) {
                    null
                }
            } else null
        }

        _allSaves.value = saves
        _currentSave.value = saves.find { it.id == config.currentSaveId } ?: saves.firstOrNull()
    }

    private fun saveTrackerConfig() {
        val meta = Tracker(
            saves = _allSaves.value.map { it.id }, currentSaveId = _currentSave.value?.id
        )
        val configFile = getTrackerConfigFile()
        val jsonString = json.encodeToString(meta)

        configFile.writeText(jsonString)
    }

    private fun writeSaveToFile(save: Save) {
        val file = getSaveFile(save.id)
        val jsonString = json.encodeToString(save)
        file.writeText(jsonString)
    }

    private fun deleteSaveFile(save: Save) {
        val file = getSaveFile(save.id)
        if (file.exists()) {
            file.delete()
        }
    }

    override suspend fun getAllSaves(): Flow<List<Save>> = _allSaves.asStateFlow()

    override suspend fun getCurrentSave(): Flow<Save?> = _currentSave.asStateFlow()

    override suspend fun setCurrentSave(save: Save) {
        _currentSave.value = save
        saveTrackerConfig()
    }

    override suspend fun createSave(name: String): Save {
        val newSave = Save(name = name)

        _allSaves.update { it + newSave }
        _currentSave.value = newSave

        writeSaveToFile(newSave)
        saveTrackerConfig()

        return newSave
    }

    override suspend fun deleteSave(save: Save) {
        _allSaves.update { it.filter { saved -> saved.id != save.id } }

        if (_currentSave.value?.id == save.id) {
            _currentSave.value = _allSaves.value.firstOrNull()
        }

        deleteSaveFile(save)
        saveTrackerConfig()
    }

    override suspend fun updateSave(save: Save) {
        val save = save.copy(updatedAt = Clock.System.now())

        _allSaves.update { saves ->
            saves.map { if (it.id == save.id) save else it }
        }

        if (_currentSave.value?.id == save.id) {
            _currentSave.value = save
        }

        writeSaveToFile(save)
        saveTrackerConfig()
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
            activities = current.activities.map { if (it.id == activity.id) activity else it }
                .toMutableList()
        )

        updateSave(updatedSave)
    }

    override suspend fun addActivityTemplate(name: String, color: String) {
        val current = _currentSave.value ?: return
        val newTemplate = ActivityTemplate(name = name, color = color)
        val updatedSave = current.copy(
            activityTemplates = (current.activityTemplates + newTemplate).toMutableList()
        )

        updateSave(updatedSave)
    }

    override suspend fun removeActivityTemplate(template: ActivityTemplate) {
        val current = _currentSave.value ?: return
        val updatedSave = current.copy(
            activityTemplates = current.activityTemplates.filter { it.id != template.id }
                .toMutableList()
        )

        updateSave(updatedSave)
    }

    override suspend fun updateActivityTemplate(template: ActivityTemplate) {
        val current = _currentSave.value ?: return
        val updatedSave = current.copy(
            activityTemplates = current.activityTemplates.map {
                if (it.id == template.id) template else it
            }.toMutableList()
        )

        updateSave(updatedSave)
    }

    override suspend fun getActivityTemplates(): Flow<List<ActivityTemplate>> {
        return _currentSave.asStateFlow().map { save ->
            save?.activityTemplates ?: emptyList()
        }
    }

    override suspend fun exportSave(save: Save) {
        val file = getSaveFile(save.id)

        // Ensure file exists
        if (!file.exists()) {
            writeSaveToFile(save)
        }

        withContext(Dispatchers.Main) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = android.content.Intent.createChooser(intent, "Export Save")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(chooser)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun importSave() {
        try {
            val platformFile = FileKit.openFilePicker(
                type = FileKitType.File(),
            ) ?: return

            val jsonString = platformFile.readString()
            val importedSave = json.decodeFromString<Save>(jsonString)

            writeSaveToFile(importedSave)
            _allSaves.update { saves ->
                val index = saves.indexOfFirst { it.id == importedSave.id }
                if (index != -1) {
                    saves.toMutableList().apply { set(index, importedSave) }
                } else {
                    saves + importedSave
                }
            }
            saveTrackerConfig()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val BASE_URL = "http://10.0.2.2:8000"
    override suspend fun syncSaveWithServer(save: Save) {
        val serverSave = downloadSave(save.id)

        if (serverSave != null && serverSave.updatedAt > save.updatedAt) {
            updateSave(serverSave)
        } else {
            uploadToServer(save)
        }
    }

    private suspend fun downloadSave(id: String): Save? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("$BASE_URL/api/saves/$id/")
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonString = response.body?.string()

                val save = json.decodeFromString<Save>(jsonString ?: return@withContext null)
                return@withContext save
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun uploadToServer(save: Save) = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val jsonString = json.encodeToString(save)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "${save.id}.json", jsonString.toRequestBody("application/json".toMediaType()))
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/api/saves/${save.id}/")
                .put(requestBody)
                .build()

            client.newCall(request).execute()
        } catch (e: Exception) {
        }
    }
}