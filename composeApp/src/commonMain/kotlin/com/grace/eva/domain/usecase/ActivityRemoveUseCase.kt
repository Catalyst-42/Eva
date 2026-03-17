package com.grace.eva.domain.usecase

import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository

open class ActivityRemoveUseCase(
    private val repository: ActivitiesRepository
) {
    open suspend operator fun invoke(activity: Activity) {
        repository.activityRemove(activity)
    }
}