package com.grace.eva.data.repository

import android.content.Context
import android.util.Log
import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Clock

class ActivitiesRepositoryImpl(
    val context: Context
): ActivitiesRepository {
    // Init empty save
    val activities = MutableStateFlow(
        Activities(
            Clock.System.now(),
            mutableListOf()
        )
    )

    private val activitiesFile: File by lazy {
        File(context.filesDir, "save.json")
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    init {
        loadActivities()
    }

    override fun loadActivities() {
        if (!activitiesFile.exists()) {
            Log.d("Save", "File are empty")
            // Use default empty activities
            return
        }

        // Load from save file
        val jsonString = activitiesFile.readText()
        activities.value = json.decodeFromString<Activities>(jsonString)
        Log.d("Save", jsonString)
    }

    override fun saveActivities() {
        val jsonString = json.encodeToString(activities.value)
        Log.d("Save", jsonString)

        activitiesFile.writeText(jsonString)
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
            begin = activities.value.begin,
            activities = newList
        )

        saveActivities()
    }

    override fun addNote(note: String) {
        if (activities.value.activities.isNotEmpty()) {
            activities.value.activities.last().note = note
        }

        saveActivities()
    }

    override fun deleteActivity(activity: Activity) {
        activities.update { currentActivities ->
            val newList = currentActivities.activities
                .filter { it.id != activity.id }
                .toMutableList()

            // Ensure that last activity is not ended one
            newList.lastOrNull()?.end = null

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
}