package com.grace.eva.domain.repository

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import kotlinx.coroutines.flow.Flow

// See implementations in ***Impl classes
interface ActivitiesRepository {
    fun getActivities(): Flow<Activities>
    fun loadActivities()
    fun saveActivities()
    fun newActivity(name: String, note: String = "")
    fun addNote(note: String)
    fun deleteActivity(activity: Activity)
    fun updateActivity(activity: Activity)
    suspend fun exportActivities(activities: Activities)
}