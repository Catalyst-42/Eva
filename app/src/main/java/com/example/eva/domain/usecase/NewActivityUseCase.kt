package com.example.eva.domain.usecase

import com.example.eva.domain.repository.ActivitiesRepository

class NewActivityUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke(name: String, note: String) {
        repository.newActivity(name, note)
    }
}