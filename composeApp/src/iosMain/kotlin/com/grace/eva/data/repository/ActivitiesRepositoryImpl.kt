package com.grace.eva.data.repository

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import platform.Foundation.writeToFile
import kotlin.time.Clock
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSFileManager
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.getBytes
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val SAVE_FILE_NAME = "eva_save_current.json"

class ActivitiesRepositoryImpl : ActivitiesRepository {
    private val activities = MutableStateFlow(Activities("Новое сохранение"))
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    init {
        loadActivities()
    }

    // Returns the path to the documents directory
    @OptIn(ExperimentalForeignApi::class)
    private fun getDocumentsPath(): String? {
        return NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String
    }

    // Full path to the save file
    @OptIn(ExperimentalForeignApi::class)
    private fun getSaveFilePath(): String? {
        return getDocumentsPath()?.let { "$it/$SAVE_FILE_NAME" }
    }

    // Write string to file
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun writeToFile(content: String, filePath: String): Boolean {
        val bytes = content.encodeToByteArray()
        val data = bytes.usePinned { pinned ->
            pinned.addressOf(0).let { address ->
                NSData.create(bytes = address, length = bytes.size.toULong())
            }
        }
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

    override fun loadActivities() {
        val filePath = getSaveFilePath() ?: return
        val jsonString = readFromFile(filePath)

        if (jsonString != null) {
            try {
                activities.value = json.decodeFromString<Activities>(jsonString)
            } catch (e: Exception) {
                // If file is corrupted, keep default state
            }
        }
    }

    override fun saveActivities() {
        val filePath = getSaveFilePath() ?: return
        val jsonString = json.encodeToString(activities.value)
        writeToFile(jsonString, filePath)
    }

    override fun getActivities(): Flow<Activities> = activities.asStateFlow()

    override fun newActivity(name: String, note: String) {
        // Create and add new activity
        val newActivity = Activity(name, note)
        activities.update { current ->
            val newList = current.activities.toMutableList()
            newList.add(newActivity)
            current.copy(activities = newList)
        }

        saveActivities()
    }

    override fun addNote(note: String) {
        activities.update { current ->
            val newList = current.activities.toMutableList()
            newList.lastOrNull()?.note = note
            current.copy(activities = newList)
        }
        saveActivities()
    }

    override fun deleteActivity(activity: Activity) {
        activities.update { current ->
            val newList = current.activities
                .filter { it.id != activity.id }
                .toMutableList()
            current.copy(activities = newList)
        }
        saveActivities()
    }

    override fun updateActivity(activity: Activity) {
        activities.update { current ->
            val newList = current.activities
                .map { if (it.id == activity.id) activity else it }
                .toMutableList()
            current.copy(activities = newList)
        }
        saveActivities()
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun exportActivities(activities: Activities) {
        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                val documentsPath = NSSearchPathForDirectoriesInDomains(
                    NSDocumentDirectory,
                    NSUserDomainMask,
                    true
                ).firstOrNull() as? String ?: run {
                    continuation.resumeWithException(Exception("Could not get documents directory"))
                    return@suspendCancellableCoroutine
                }

                val filePath = "$documentsPath/eva_save_current.json"

                // Check if file exists
                val fileManager = NSFileManager.defaultManager
                if (!fileManager.fileExistsAtPath(filePath)) {
                    continuation.resumeWithException(Exception("No save file found"))
                    return@suspendCancellableCoroutine
                }

                val fileURL = NSURL.fileURLWithPath(filePath)

                val activityVC = UIActivityViewController(
                    activityItems = listOf(fileURL),
                    applicationActivities = null
                )

                val keyWindow = UIApplication.sharedApplication.keyWindow
                val rootViewController = keyWindow?.rootViewController
                val topController = getTopViewController(rootViewController)

                if (topController != null) {
                    topController.presentViewController(activityVC, true, null)
                    continuation.resume(Unit)
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