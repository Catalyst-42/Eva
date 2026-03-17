package com.grace.eva.domain.usecase.sync

import com.grace.eva.domain.repository.TrackerRepository

open class ImportSaveUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke() = repository.importSave()
}