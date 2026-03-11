package com.grace.eva.domain.usecase

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.repository.ActivitiesRepository
import kotlinx.coroutines.flow.Flow

class GetActivitiesUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke(): Flow<Activities> = repository.getActivities()
}