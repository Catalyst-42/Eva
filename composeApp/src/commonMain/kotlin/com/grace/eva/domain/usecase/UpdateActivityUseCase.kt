package com.grace.eva.domain.usecase

import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository

class UpdateActivityUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke(activity: Activity) {
        repository.updateActivity(activity)
    }
}