package com.grace.eva.di

import com.grace.eva.data.repository.ActivitiesRepositoryImpl
import com.grace.eva.domain.repository.ActivitiesRepository
import com.grace.eva.domain.usecase.AddNoteToLastActivityUseCase
import com.grace.eva.domain.usecase.DeleteLastActivityUseCase
import com.grace.eva.domain.usecase.GetActivitiesUseCase
import com.grace.eva.domain.usecase.NewActivityUseCase
import com.grace.eva.domain.usecase.SaveActivitiesUseCase

class IosAppContainer : AppContainer {
    private val activitiesRepository: ActivitiesRepository =
        ActivitiesRepositoryImpl()  // Заметили? Нет Context!

    override val getActivitiesUseCase: GetActivitiesUseCase =
        GetActivitiesUseCase(activitiesRepository)
    override val newActivityUseCase: NewActivityUseCase =
        NewActivityUseCase(activitiesRepository)
    override val deleteLastActivityUseCase: DeleteLastActivityUseCase =
        DeleteLastActivityUseCase(activitiesRepository)
    override val addNoteToLastActivityUseCase: AddNoteToLastActivityUseCase =
        AddNoteToLastActivityUseCase(activitiesRepository)
    override val saveActivitiesUseCase: SaveActivitiesUseCase =
        SaveActivitiesUseCase(activitiesRepository)
}

actual fun createAppContainer(): AppContainer = IosAppContainer()
