package com.grace.eva.domain.repository

import com.grace.eva.domain.model.Save
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.domain.model.ConnectionCheckResult
import com.grace.eva.domain.model.Tracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockTrackerRepository : TrackerRepository {
    override suspend fun getTracker(): Flow<Tracker> = flowOf(Tracker())
    override suspend fun getAllSaves(): Flow<List<Save>> = flowOf(emptyList())
    override suspend fun getCurrentSave(): Flow<Save?> = flowOf(null)
    override suspend fun setCurrentSave(save: Save) {}

    override suspend fun createSave(name: String): Save = Save(name = name)
    override suspend fun deleteSave(save: Save) {}
    override suspend fun updateSave(save: Save) {}
    override suspend fun reorderSaves(saves: List<Save>) {}

    override suspend fun addActivity(name: String) {}
    override suspend fun removeActivity(activity: Activity) {}
    override suspend fun updateActivity(activity: Activity) {}

    override suspend fun addActivityTemplate(name: String, color: String) { }
    override suspend fun removeActivityTemplate(template: ActivityTemplate) { }
    override suspend fun updateActivityTemplate(template: ActivityTemplate) { }
    override suspend fun reorderActivityTemplates(templates: List<ActivityTemplate>) { }
    override suspend fun getActivityTemplates(): Flow<List<ActivityTemplate>> = flowOf(emptyList())

    override suspend fun exportSave(save: Save) {}
    override suspend fun importSave() {}
    override suspend fun updateRemoteServerUrl(url: String) {}
    override suspend fun testRemoteServerConnection(): ConnectionCheckResult =
        ConnectionCheckResult("Сервер", true, "Мок-соединение доступно")

    override suspend fun testRemoteSaveApiConnection(): ConnectionCheckResult =
        ConnectionCheckResult("API сохранений", true, "Мок-API доступно")

    override suspend fun syncSaveWithServer(save: Save) {}
}
