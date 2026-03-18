package com.grace.eva.data.repository

import com.grace.eva.domain.model.Save
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import platform.Foundation.writeToFile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSFileManager
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.getBytes
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIViewController
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import platform.UniformTypeIdentifiers.UTType
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject
import platform.UIKit.UIDocumentPickerDelegateProtocol
import kotlin.time.Clock
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

private const val TRACKER_CONFIG_FILE = "tracker.json"
private const val SAVES_DIRECTORY = "saves"

@Serializable
data class TrackerMeta(
    val saves: List<String> = emptyList(),
    val currentSaveId: String? = null
)

class TrackerRepositoryImpl : TrackerRepository {
    private val _allSaves = MutableStateFlow<List<Save>>(emptyList())
    private val _currentSave = MutableStateFlow<Save?>(null)
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    init {
        loadTrackerData()
    }

    private fun triggerHaptic() {
        val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleSoft)
        generator.prepare()
        generator.impactOccurred()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getDocumentsPath(): String? {
        return NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
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
                savesDirPath,
                true,
                null,
                null
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
                json.decodeFromString<TrackerMeta>(metaJson)
            } catch (e: Exception) {
                TrackerMeta()
            }
        } else {
            TrackerMeta()
        }

        val saves = meta.saves.mapNotNull { saveId ->
            val saveFilePath = getSaveFilePath(saveId) ?: return@mapNotNull null
            val saveJson = readFromFile(saveFilePath)
            if (saveJson != null) {
                try {
                    json.decodeFromString<Save>(saveJson)
                } catch (e: Exception) {
                    null
                }
            } else null
        }

        _allSaves.value = saves
        _currentSave.value = saves.find { it.id == meta.currentSaveId } ?: saves.firstOrNull()
    }

    private suspend fun saveTrackerMeta() {
        val metaFilePath = getTrackerMetaFilePath() ?: return
        val meta = TrackerMeta(
            saves = _allSaves.value.map { it.id },
            currentSaveId = _currentSave.value?.id
        )
        val jsonString = json.encodeToString(meta)
        writeToFile(jsonString, metaFilePath)
    }

    private suspend fun saveSaveToFile(save: Save) {
        ensureSavesDirectoryExists()
        val filePath = getSaveFilePath(save.id) ?: return
        val jsonString = json.encodeToString(save)
        writeToFile(jsonString, filePath)
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun deleteSaveFile(save: Save) {
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

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun exportSave(save: Save) {
        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                // Ensure the save file exists
                val saveFilePath = getSaveFilePath(save.id)
                if (saveFilePath == null) {
                    continuation.resumeWithException(Exception("Could not get save file path"))
                    return@suspendCancellableCoroutine
                }

                // Check if file exists
                val fileManager = NSFileManager.defaultManager
                if (!fileManager.fileExistsAtPath(saveFilePath)) {
                    // Create temp file for export if it doesn't exist
                    val jsonString = json.encodeToString(save)
                    writeToFile(jsonString, saveFilePath)
                }

                val fileURL = NSURL.fileURLWithPath(saveFilePath)

                val activityVC = UIActivityViewController(
                    activityItems = listOf(fileURL),
                    applicationActivities = null
                )

                val keyWindow = UIApplication.sharedApplication.keyWindow
                val rootViewController = keyWindow?.rootViewController
                val topController = getTopViewController(rootViewController)

                if (topController != null) {
                    topController.presentViewController(
                        activityVC,
                        true,
                        null
                    )
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(Exception("Could not find top view controller"))
                }

            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun importSave() {
        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                val documentPicker = UIDocumentPickerViewController(
                    documentTypes = listOf("public.json"),
                    inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
                ).apply {
                    allowsMultipleSelection = false

                    val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: run {
                                continuation.resumeWithException(Exception("No file selected"))
                                return
                            }

                            val didStartAccessing = url.startAccessingSecurityScopedResource()

                            try {
                                val path = url.path ?: throw Exception("Invalid file path")
                                val jsonString = NSData.dataWithContentsOfFile(path)?.let { data ->
                                    val bytes = ByteArray(data.length.toInt())
                                    bytes.usePinned { pinned ->
                                        data.getBytes(pinned.addressOf(0), data.length)
                                    }
                                    bytes.decodeToString()
                                } ?: throw Exception("Could not read file")

                                val importedSave = json.decodeFromString<Save>(jsonString)

                                val newSave = Save(
                                    name = importedSave.name,
                                    activities = importedSave.activities.map { activity ->
                                        Activity(
                                            name = activity.name,
                                            note = activity.note,
                                            begin = activity.begin
                                        )
                                    }.toMutableList(),
                                    end = importedSave.end
                                )

                                val existingWithSameName = _allSaves.value.find { it.name == newSave.name }
                                val finalName = if (existingWithSameName != null) {
                                    "${newSave.name} (${Clock.System.now().epochSeconds})"
                                } else {
                                    newSave.name
                                }

                                val saveToAdd = newSave.copy(name = finalName)

                                // Launch coroutine for suspend functions
                                @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                                GlobalScope.launch {
                                    try {
                                        ensureSavesDirectoryExists()
                                        saveSaveToFile(saveToAdd)
                                        _allSaves.value += saveToAdd
                                        saveTrackerMeta()
                                        continuation.resume(Unit)
                                    } catch (e: Exception) {
                                        continuation.resumeWithException(e)
                                    }
                                }

                            } catch (e: Exception) {
                                continuation.resumeWithException(e)
                            } finally {
                                if (didStartAccessing) {
                                    url.stopAccessingSecurityScopedResource()
                                }
                            }
                        }

                        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                            continuation.resumeWithException(Exception("Import cancelled"))
                        }
                    }

                    this.delegate = delegate
                }

                val keyWindow = UIApplication.sharedApplication.keyWindow
                val rootViewController = keyWindow?.rootViewController
                val topController = getTopViewController(rootViewController)

                if (topController != null) {
                    topController.presentViewController(documentPicker, true, null)
                } else {
                    continuation.resumeWithException(Exception("Could not find top view controller"))
                }

            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    private fun getTopViewController(controller: UIViewController?): UIViewController? {
        if (controller == null) return null
        if (controller.presentedViewController != null) {
            return getTopViewController(controller.presentedViewController)
        }
        return controller
    }
}