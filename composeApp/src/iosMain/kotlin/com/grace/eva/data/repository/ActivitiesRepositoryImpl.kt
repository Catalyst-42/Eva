package com.grace.eva.data.repository

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
            begin = activities.value.begin,
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
}