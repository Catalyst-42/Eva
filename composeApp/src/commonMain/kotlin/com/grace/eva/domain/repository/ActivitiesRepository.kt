package com.grace.eva.domain.repository

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import kotlinx.coroutines.flow.Flow

// See implementations in ***Impl classes
interface ActivitiesRepository {
    fun activitiesLoad()
    fun activitiesGet(): Flow<Activities>
    fun activitiesSave()
    fun activityNew(name: String, note: String = "")
    fun activityRemove(activity: Activity)
    fun activityUpdate(activity: Activity)
    suspend fun activitiesExport(activities: Activities)
    suspend fun activitiesRename(name: String)
}