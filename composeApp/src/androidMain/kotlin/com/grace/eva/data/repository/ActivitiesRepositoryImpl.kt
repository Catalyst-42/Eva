package com.grace.eva.data.repository

import android.content.Context
import android.util.Log
import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        activities.value.activities.add(newActivity)

        saveActivities()
    }

    override fun addNote(note: String) {
        if (activities.value.activities.isNotEmpty()) {
            activities.value.activities.last().note = note
        }

        saveActivities()
    }

    override fun deleteLastActivity() {
        if (activities.value.activities.isNotEmpty()) {
            val currentList = activities.value.activities.toMutableList()
            currentList.removeAt(currentList.lastIndex)

            // Make previous activity active now
            if (currentList.isNotEmpty()) {
                currentList.last().end = null
            }

            activities.value = Activities(
                begin = Clock.System.now(),
                activities = currentList
            )

            saveActivities()
        }
    }
}