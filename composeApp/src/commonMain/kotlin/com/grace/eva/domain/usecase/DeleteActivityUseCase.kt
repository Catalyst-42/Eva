package com.grace.eva.domain.usecase

import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository

class DeleteActivityUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke(activity: Activity) {
        repository.deleteActivity(activity)
    }
}