package com.grace.eva.domain.repository

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockActivitiesRepository : ActivitiesRepository {
    override fun activitiesGet(): Flow<Activities> = flowOf(Activities())
    override fun activitiesLoad() { }
    override fun activitiesSave() { }
    override fun activityNew(name: String, note: String) { }
    override fun activityRemove(activity: Activity) { }
    override fun activityUpdate(activity: Activity) { }
    override suspend fun activitiesExport(activities: Activities) { }
    override suspend fun activitiesRename(name: String) { }
}
