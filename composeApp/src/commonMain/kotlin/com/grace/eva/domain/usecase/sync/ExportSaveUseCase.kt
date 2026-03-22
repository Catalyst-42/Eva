package com.grace.eva.domain.usecase.sync

import com.grace.eva.domain.model.Save
import com.grace.eva.domain.repository.TrackerRepository

open class ExportSaveUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke(save: Save) {
        repository.exportSave(save)
    }
}