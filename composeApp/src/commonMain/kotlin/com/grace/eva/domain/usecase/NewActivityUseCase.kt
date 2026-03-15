package com.grace.eva.domain.usecase

import com.grace.eva.domain.repository.ActivitiesRepository

open class NewActivityUseCase(
    private val repository: ActivitiesRepository
) {
    open suspend operator fun invoke(name: String, note: String) {
        repository.newActivity(name, note)
    }
}