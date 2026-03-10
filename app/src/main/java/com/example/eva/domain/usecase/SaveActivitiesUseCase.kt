package com.example.eva.domain.usecase

import com.example.eva.domain.repository.ActivitiesRepository

class SaveActivitiesUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke() {
        repository.saveActivities()
    }
}