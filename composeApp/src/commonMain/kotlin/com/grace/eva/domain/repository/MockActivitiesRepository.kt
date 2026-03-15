package com.grace.eva.domain.repository

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockActivitiesRepository : ActivitiesRepository {
    override fun getActivities(): Flow<Activities> = flowOf(Activities())
    override fun loadActivities() { }
    override fun saveActivities() { }
    override fun newActivity(name: String, note: String) { }
    override fun addNote(note: String) { }
    override fun deleteActivity(activity: Activity) { }
    override fun updateActivity(activity: Activity) { }
    override suspend fun exportActivities(activities: Activities) { }
}
