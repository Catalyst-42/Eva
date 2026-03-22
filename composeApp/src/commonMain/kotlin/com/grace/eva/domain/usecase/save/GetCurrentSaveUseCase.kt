package com.grace.eva.domain.usecase.save

import com.grace.eva.domain.model.Save
import com.grace.eva.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow

open class GetCurrentSaveUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke(): Flow<Save?> {
        return repository.getCurrentSave()
    }
}