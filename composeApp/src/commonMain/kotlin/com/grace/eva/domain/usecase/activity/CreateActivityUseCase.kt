package com.grace.eva.domain.usecase.activity

import com.grace.eva.domain.repository.TrackerRepository

open class CreateActivityUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke(name: String) = repository.addActivity(name)
}