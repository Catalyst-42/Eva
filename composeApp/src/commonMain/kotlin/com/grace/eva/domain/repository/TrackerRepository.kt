package com.grace.eva.domain.repository

import com.grace.eva.domain.model.Save
import com.grace.eva.domain.model.Activity
import kotlinx.coroutines.flow.Flow

// See implementations in *Impl classes
interface TrackerRepository {
    suspend fun getAllSaves(): Flow<List<Save>>
    suspend fun getCurrentSave(): Flow<Save?>
    suspend fun setCurrentSave(save: Save)

    suspend fun createSave(name: String): Save
    suspend fun deleteSave(save: Save)
    suspend fun updateSave(save: Save)

    suspend fun addActivity(name: String)
    suspend fun removeActivity(activity: Activity)
    suspend fun updateActivity(activity: Activity)

    suspend fun exportSave(save: Save)
    suspend fun importSave()
}