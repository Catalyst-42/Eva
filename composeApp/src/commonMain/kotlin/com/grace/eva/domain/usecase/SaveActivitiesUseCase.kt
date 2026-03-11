package com.grace.eva.domain.usecase

import com.grace.eva.domain.repository.ActivitiesRepository

class SaveActivitiesUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke() {
        repository.saveActivities()
    }
}