package com.grace.eva.domain.usecase

import com.grace.eva.domain.repository.ActivitiesRepository

open class ActivitiesSaveUseCase(
    private val repository: ActivitiesRepository
) {
    open suspend operator fun invoke() {
        repository.activitiesSave()
    }
}