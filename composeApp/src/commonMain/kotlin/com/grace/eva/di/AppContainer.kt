package com.grace.eva.di

import com.grace.eva.domain.usecase.AddNoteToLastActivityUseCase
import com.grace.eva.domain.usecase.DeleteLastActivityUseCase
import com.grace.eva.domain.usecase.GetActivitiesUseCase
import com.grace.eva.domain.usecase.NewActivityUseCase
import com.grace.eva.domain.usecase.SaveActivitiesUseCase

interface AppContainer {
    val getActivitiesUseCase: GetActivitiesUseCase
    val newActivityUseCase: NewActivityUseCase
    val deleteLastActivityUseCase: DeleteLastActivityUseCase
    val addNoteToLastActivityUseCase: AddNoteToLastActivityUseCase
    val saveActivitiesUseCase: SaveActivitiesUseCase
}

expect fun createAppContainer(): AppContainer