package com.grace.eva.domain.usecase

import com.grace.eva.domain.repository.ActivitiesRepository

open class AddNoteToLastActivityUseCase(
    private val repository: ActivitiesRepository
) {
    open suspend operator fun invoke(note: String) {
        repository.addNote(note)
    }
}