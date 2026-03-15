package com.grace.eva.di

import com.grace.eva.domain.usecase.ActivitiesExportUseCase
import com.grace.eva.domain.usecase.AddNoteToLastActivityUseCase
import com.grace.eva.domain.usecase.DeleteActivityUseCase
import com.grace.eva.domain.usecase.GetActivitiesUseCase
import com.grace.eva.domain.usecase.NewActivityUseCase
import com.grace.eva.domain.usecase.SaveActivitiesUseCase
import com.grace.eva.domain.usecase.UpdateActivityUseCase

interface AppContainer {
    val getActivitiesUseCase: GetActivitiesUseCase
    val newActivityUseCase: NewActivityUseCase
    val deleteActivityUseCase: DeleteActivityUseCase
    val addNoteToLastActivityUseCase: AddNoteToLastActivityUseCase
    val saveActivitiesUseCase: SaveActivitiesUseCase
    val updateActivityUseCase: UpdateActivityUseCase
    val activitiesExportUseCase: ActivitiesExportUseCase
}

expect fun createAppContainer(): AppContainer