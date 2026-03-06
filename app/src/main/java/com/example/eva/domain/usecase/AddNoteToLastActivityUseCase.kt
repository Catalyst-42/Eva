package com.example.eva.domain.usecase

import com.example.eva.domain.repository.ActivitiesRepository

class AddNoteToLastActivityUseCase(
    private val repository: ActivitiesRepository
) {
    operator fun invoke(note: String) {
        repository.addNote(note)
    }
}