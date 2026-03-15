package com.grace.eva.domain.usecase

import com.grace.eva.domain.repository.ActivitiesRepository

open class SaveActivitiesUseCase(
    private val repository: ActivitiesRepository
) {
    open suspend operator fun invoke() {
        repository.saveActivities()
    }
}