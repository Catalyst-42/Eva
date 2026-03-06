package com.example.eva.domain.usecase

import com.example.eva.domain.repository.ActivitiesRepository

class DeleteLastActivityUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke() {
        repository.deleteLastActivity()
    }
}