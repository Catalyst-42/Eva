package com.grace.eva.domain.usecase

import com.grace.eva.domain.repository.ActivitiesRepository

class AddNoteToLastActivityUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke(note: String) {
        repository.addNote(note)
    }
}