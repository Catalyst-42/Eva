package com.example.eva.domain.usecase

import com.example.eva.domain.model.Activities
import com.example.eva.domain.repository.ActivitiesRepository
import kotlinx.coroutines.flow.Flow

class GetActivitiesUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke(): Flow<Activities> = repository.getActivities()
}