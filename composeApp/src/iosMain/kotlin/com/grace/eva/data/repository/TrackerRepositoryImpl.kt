package com.grace.eva.data.repository

import com.grace.eva.di.IosRootController
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.domain.model.Save
import com.grace.eva.domain.model.Tracker
import com.grace.eva.domain.repository.TrackerRepository
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSItemProvider
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.getBytes
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import kotlin.time.Clock

private const val TRACKER_CONFIG_FILE = "tracker.json"
private const val SAVES_DIRECTORY = "saves"

class TrackerRepositoryImpl : TrackerRepository {
    private val _allSaves = MutableStateFlow<List<Save>>(emptyList())
    private val _currentSave = MutableStateFlow<Save?>(null)
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    init {
        loadTrackerData()
    }

    private fun triggerHaptic() {
        val generator = UIImpactFeedbackGenerator(
            style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
        )
        generator.prepare()
        generator.impactOccurred()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getDocumentsPath(): String? {
        return NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() as? String
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getTrackerMetaFilePath(): String? {
        return getDocumentsPath()?.let { "$it/$TRACKER_CONFIG_FILE" }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getSavesDirectoryPath(): String? {
        return getDocumentsPath()?.let { "$it/$SAVES_DIRECTORY" }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getSaveFilePath(saveId: String): String? {
        return getSavesDirectoryPath()?.let { "$it/$saveId.json" }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ensureSavesDirectoryExists() {
        val savesDirPath = getSavesDirectoryPath() ?: return
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(savesDirPath)) {
            fileManager.createDirectoryAtPath(
                savesDirPath, true, null, null
            )
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun writeToFile(content: String, filePath: String): Boolean {
        val bytes = content.encodeToByteArray()
        val data = bytes.usePinned { pinned ->
            pinned.addressOf(0).let { address ->
                NSData.create(bytes = address, length = bytes.size.toULong())
            }
        }
        triggerHaptic()
        return data.writeToFile(filePath, true)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun readFromFile(filePath: String): String? {
        return NSData.dataWithContentsOfFile(filePath)?.let { data ->
            val byteArray = ByteArray(data.length.toInt())
            byteArray.usePinned { pinned ->
                data.getBytes(pinned.addressOf(0), data.length)
            }
            byteArray.decodeToString()
        }
    }

    private fun loadTrackerData() {
        val metaFilePath = getTrackerMetaFilePath() ?: return
        val metaJson = readFromFile(metaFilePath)

        val meta = if (metaJson != null) {
            try {
                json.decodeFromString<Tracker>(metaJson)
            } catch (_: Exception) {
                Tracker()
            }
        } else {
            Tracker()
        }

        val saves = meta.saves.mapNotNull { saveId ->
            val saveFilePath = getSaveFilePath(saveId) ?: return@mapNotNull null
            val saveJson = readFromFile(saveFilePath)
            if (saveJson != null) {
                try {
                    json.decodeFromString<Save>(saveJson)
                } catch (_: Exception) {
                    null
                }
            } else null
        }

        _allSaves.value = saves
        _currentSave.value = saves.find { it.id == meta.currentSaveId } ?: saves.firstOrNull()
    }

    private fun saveTrackerConfig() {
        val configFilePath = getTrackerMetaFilePath() ?: return
        val config = Tracker(
            saves = _allSaves.value.map { it.id }, currentSaveId = _currentSave.value?.id
        )
        val jsonString = json.encodeToString(config)
        writeToFile(jsonString, configFilePath)
    }

    private fun writeSaveToFile(save: Save) {
        ensureSavesDirectoryExists()
        val filePath = getSaveFilePath(save.id) ?: return
        val jsonString = json.encodeToString(save)
        writeToFile(jsonString, filePath)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun deleteSaveFile(save: Save) {
        val filePath = getSaveFilePath(save.id) ?: return
        val fileManager = NSFileManager.defaultManager
        if (fileManager.fileExistsAtPath(filePath)) {
            fileManager.removeItemAtPath(filePath, null)
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
        val updatedSave =
            current.copy(activities = current.activities.map { if (it.id == activity.id) activity else it }
                .toMutableList())

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
        val updatedSave =
            current.copy(activityTemplates = current.activityTemplates.filter { it.id != template.id }
                .toMutableList())

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
        val filePath = getSaveFilePath(save.id) ?: return

        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(filePath)) {
            writeSaveToFile(save)
        }

        val fileURL = NSURL.fileURLWithPath(filePath)

        val activityVC = UIActivityViewController(
            activityItems = listOf(fileURL),
            applicationActivities = null
        )

        withContext(Dispatchers.Main) {
            delay(100)

            val topController = IosRootController.rootViewController ?: return@withContext

            topController.presentViewController(
                viewControllerToPresent = activityVC,
                animated = true,
                completion = null
            )
        }
    }

    override suspend fun importSave() {
        // TODO: Make
    }

    private fun getTopViewController(controller: UIViewController?): UIViewController? {
        if (controller == null) return null
        if (controller.presentedViewController != null) {
            return getTopViewController(controller.presentedViewController)
        }
        return controller
    }
}