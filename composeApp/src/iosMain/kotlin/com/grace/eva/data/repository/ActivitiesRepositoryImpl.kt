package com.grace.eva.data.repository

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults
import kotlin.time.Clock

class ActivitiesRepositoryImpl(): ActivitiesRepository {
    private val activities = MutableStateFlow(
        Activities(Clock.System.now(), mutableListOf())
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
                println("iOS: Loaded from UserDefaults")
            } catch (e: Exception) {
                println("iOS: Error loading: ${e.message}")
            }
        } else {
            println("iOS: No saved data found")
        }
    }

    override fun saveActivities() {
        val jsonString = json.encodeToString(activities.value)
        userDefaults.setObject(jsonString, saveKey)
        println("iOS: Saved to UserDefaults")
    }

    override fun getActivities(): Flow<Activities> {
        return activities.asStateFlow()
    }

    override fun newActivity(name: String, note: String) {
        // End previous one
        if (activities.value.activities.isNotEmpty()) {
            activities.value.activities.last().end = Clock.System.now()
        }

        // Create new one
        val newActivity = Activity(name, note)
        activities.value.activities.add(newActivity)

        saveActivities()
    }

    override fun addNote(note: String) {
        if (activities.value.activities.isNotEmpty()) {
            activities.value.activities.last().note = note
            saveActivities()
        }
    }

    override fun deleteLastActivity() {
        if (activities.value.activities.isNotEmpty()) {
            val currentList = activities.value.activities.toMutableList()
            currentList.removeLast()

            // Если остались активности, обновляем время последней
            if (currentList.isNotEmpty()) {
                currentList.last().end = Clock.System.now()
            }

            activities.value = Activities(
                begin = Clock.System.now(),
                activities = currentList
            )

            saveActivities()
            println("iOS: Last activity deleted")
        }
    }
}