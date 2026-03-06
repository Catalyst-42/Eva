package com.example.eva.domain.repository

import com.example.eva.domain.model.Activities
import kotlinx.coroutines.flow.Flow

interface ActivitiesRepository {
    fun loadActivities()
    fun saveActivities()
    fun getActivities(): Flow<Activities>
    fun newActivity(name: String, note: String = "")
    fun addNote(note: String)
    fun deleteLastActivity()
}