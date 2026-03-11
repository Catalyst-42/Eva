package com.grace.eva.domain.usecase

import com.grace.eva.domain.repository.ActivitiesRepository

class NewActivityUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke(name: String, note: String) {
        repository.newActivity(name, note)
    }
}