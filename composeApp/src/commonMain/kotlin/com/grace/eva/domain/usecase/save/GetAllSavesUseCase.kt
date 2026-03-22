package com.grace.eva.domain.usecase.save

import com.grace.eva.domain.model.Save
import com.grace.eva.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow

open class GetAllSavesUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke(): Flow<List<Save>> {
        return repository.getAllSaves()
    }
}