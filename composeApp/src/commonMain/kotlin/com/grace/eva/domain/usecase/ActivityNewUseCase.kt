package com.grace.eva.domain.usecase

import com.grace.eva.domain.repository.ActivitiesRepository

open class ActivityNewUseCase(
    private val repository: ActivitiesRepository
) {
    open suspend operator fun invoke(name: String, note: String) {
        repository.activityNew(name, note)
    }
}