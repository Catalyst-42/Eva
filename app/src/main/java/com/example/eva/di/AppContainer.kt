package com.example.eva.di

import android.content.Context
import com.example.eva.data.repository.ActivitiesRepositoryImpl
import com.example.eva.domain.repository.ActivitiesRepository
import com.example.eva.domain.usecase.AddNoteToLastActivityUseCase
import com.example.eva.domain.usecase.DeleteLastActivityUseCase
import com.example.eva.domain.usecase.GetActivitiesUseCase
import com.example.eva.domain.usecase.NewActivityUseCase

/**
 * DI class
 *
 * Used to define all use cases to be used lately in
 * view functions
 */
class AppContainer(context: Context) {
    private val activitiesRepository: ActivitiesRepository = ActivitiesRepositoryImpl(context)

    // Use cases
    val getActivitiesUseCase: GetActivitiesUseCase = GetActivitiesUseCase(activitiesRepository)
    val newActivityUseCase: NewActivityUseCase = NewActivityUseCase(activitiesRepository)
    val deleteLastActivityUseCase: DeleteLastActivityUseCase = DeleteLastActivityUseCase(activitiesRepository)
    val addNoteToLastActivityUseCase: AddNoteToLastActivityUseCase = AddNoteToLastActivityUseCase(activitiesRepository)
}