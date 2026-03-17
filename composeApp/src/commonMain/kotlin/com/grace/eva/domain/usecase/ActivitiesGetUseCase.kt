package com.grace.eva.domain.usecase

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.repository.ActivitiesRepository
import kotlinx.coroutines.flow.Flow

open class ActivitiesGetUseCase(
    private val repository: ActivitiesRepository
) {
    open operator fun invoke(): Flow<Activities> = repository.activitiesGet()
}