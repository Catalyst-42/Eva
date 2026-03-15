package com.grace.eva.di

import com.grace.eva.data.repository.ActivitiesRepositoryImpl
import com.grace.eva.domain.repository.ActivitiesRepository
import com.grace.eva.domain.usecase.ActivitiesExportUseCase
import com.grace.eva.domain.usecase.AddNoteToLastActivityUseCase
import com.grace.eva.domain.usecase.DeleteActivityUseCase
import com.grace.eva.domain.usecase.GetActivitiesUseCase
import com.grace.eva.domain.usecase.NewActivityUseCase
import com.grace.eva.domain.usecase.SaveActivitiesUseCase
import com.grace.eva.domain.usecase.UpdateActivityUseCase

class IosAppContainer : AppContainer {
    private val activitiesRepository: ActivitiesRepository =
        ActivitiesRepositoryImpl()

    override val getActivitiesUseCase: GetActivitiesUseCase =
        GetActivitiesUseCase(activitiesRepository)
    override val newActivityUseCase: NewActivityUseCase =
        NewActivityUseCase(activitiesRepository)
    override val deleteActivityUseCase: DeleteActivityUseCase =
        DeleteActivityUseCase(activitiesRepository)
    override val addNoteToLastActivityUseCase: AddNoteToLastActivityUseCase =
        AddNoteToLastActivityUseCase(activitiesRepository)
    override val saveActivitiesUseCase: SaveActivitiesUseCase =
        SaveActivitiesUseCase(activitiesRepository)
    override val updateActivityUseCase: UpdateActivityUseCase =
        UpdateActivityUseCase(activitiesRepository)
    override val activitiesExportUseCase: ActivitiesExportUseCase =
        ActivitiesExportUseCase(activitiesRepository)
}

actual fun createAppContainer(): AppContainer = IosAppContainer()
