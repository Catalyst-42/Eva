package com.grace.eva.data.repository

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import kotlin.coroutines.resumeWithException
import kotlin.time.Clock
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import platform.Foundation.writeToFile
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ActivitiesRepositoryImpl(): ActivitiesRepository {
    private val activities = MutableStateFlow(
        Activities("Новое сохранение", mutableListOf())
    )

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val saveKey = "activities_save"

    init {
        loadActivities()
    }

    override fun loadActivities() {
        val jsonString = userDefaults.stringForKey(saveKey)
        if (jsonString != null) {
            try {
                activities.value = json.decodeFromString<Activities>(jsonString)
            } catch (e: Exception) {
            }
        }
    }

    override fun saveActivities() {
        val jsonString = json.encodeToString(activities.value)
        userDefaults.setObject(jsonString, saveKey)
    }

    override fun getActivities(): Flow<Activities> {
        return activities.asStateFlow()
    }

    override fun newActivity(name: String, note: String) {
        // End previous activity
        if (activities.value.activities.isNotEmpty()) {
            activities.value.activities.last().end = Clock.System.now()
        }

        // Create new activity
        val newActivity = Activity(name, note)
        val newList = activities.value.activities.toMutableList()
        newList.add(newActivity)

        activities.value = Activities(
            activities = newList
        )

        saveActivities()
    }

    override fun addNote(note: String) {
        if (activities.value.activities.isNotEmpty()) {
            activities.value.activities.last().note = note
            saveActivities()
        }

        saveActivities()
    }

    override fun deleteActivity(activity: Activity) {
        activities.update { currentActivities ->
            val newList = currentActivities.activities
                .filter { it.id != activity.id }
                .toMutableList()

            currentActivities.copy(activities = newList)
        }

        saveActivities()
    }

    override fun updateActivity(activity: Activity) {
        activities.update { currentActivities ->
            val newList = currentActivities.activities
                .mapTo(mutableListOf()) { if (it.id == activity.id) activity else it }

            currentActivities.copy(activities = newList)
        }

        saveActivities()
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun exportActivities(activities: Activities) {
        val jsonString = try {
            json.encodeToString(activities)
        } catch (e: Exception) {
            return
        }

        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                // Get documents directory
                val documentsPath = NSSearchPathForDirectoriesInDomains(
                    NSDocumentDirectory,
                    NSUserDomainMask,
                    true
                ).firstOrNull() as? String ?: run {
                    continuation.resumeWithException(Exception("Could not get documents directory"))
                    return@suspendCancellableCoroutine
                }

                val fileName = "eva_save_${Clock.System.now().toEpochMilliseconds()}.json"
                val filePath = "$documentsPath/$fileName"

                // Convert string to NSData
                val bytes = jsonString.encodeToByteArray()
                val data = bytes.usePinned { pinned ->
                    pinned.addressOf(0).let { address ->
                        NSData.create(bytes = address, length = bytes.size.toULong())
                    }
                }

                // Write to file
                val success = data.writeToFile(filePath, true)
                if (!success) {
                    continuation.resumeWithException(Exception("Failed to write file"))
                    return@suspendCancellableCoroutine
                }

                // Create file URL for sharing
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