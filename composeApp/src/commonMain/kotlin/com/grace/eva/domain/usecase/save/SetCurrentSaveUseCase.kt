package com.grace.eva.domain.usecase.save

import com.grace.eva.domain.model.Save
import com.grace.eva.domain.repository.TrackerRepository

open class SetCurrentSaveUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke(save: Save) {
        repository.setCurrentSave(save)
    }
}