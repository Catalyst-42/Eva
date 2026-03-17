package com.grace.eva.domain.usecase.save

import com.grace.eva.domain.model.Save
import com.grace.eva.domain.repository.TrackerRepository

open class CreateSaveUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke(name: String): Save = repository.createSave(name)
}