package com.example.eva.data.repository

import android.content.Context
import com.example.eva.domain.model.Activities
import com.example.eva.domain.model.Activity
import com.example.eva.domain.repository.ActivitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.time.Clock

class ActivitiesRepositoryImpl (
    val context: Context
): ActivitiesRepository {
    // Init empty save
    val activities = MutableStateFlow(
       Activities(Clock.System.now(),
       mutableListOf())
    )

    init {
        loadActivities()
    }

    override fun loadActivities() {
       val file = File(context.filesDir, "activities.json")

        if (!file.exists()) {
            // Use default empty activities
            return
        }

        // TODO: make loading from json file
    }

    override fun saveActivities() {
        // TODO: Make saving in json file
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
    }

    override fun addNote(note: String) {
        if (activities.value.activities.isNotEmpty()) {
            activities.value.activities.last().note = note
        }
    }

    override fun deleteLastActivity() {
        if (activities.value.activities.isNotEmpty()) {
            activities.value.activities.dropLast(1)
        }
    }
}