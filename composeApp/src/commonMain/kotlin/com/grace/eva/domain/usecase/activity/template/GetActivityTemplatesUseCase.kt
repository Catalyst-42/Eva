package com.grace.eva.domain.usecase.activity.template

import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow

open class GetActivityTemplatesUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke(): Flow<List<ActivityTemplate>> {
        return repository.getActivityTemplates()
    }
}