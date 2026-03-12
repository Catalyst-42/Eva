package com.grace.eva.di

import android.content.Context
import com.grace.eva.data.repository.ActivitiesRepositoryImpl
import com.grace.eva.domain.repository.ActivitiesRepository
import com.grace.eva.domain.usecase.AddNoteToLastActivityUseCase
import com.grace.eva.domain.usecase.DeleteActivityUseCase
import com.grace.eva.domain.usecase.GetActivitiesUseCase
import com.grace.eva.domain.usecase.NewActivityUseCase
import com.grace.eva.domain.usecase.SaveActivitiesUseCase
import com.grace.eva.domain.usecase.UpdateActivityUseCase

/**
 * DI class
 *
 * Used to define all use cases to be used lately in
 * view functions
 */
class AndroidAppContainer(context: Context): AppContainer {
    private val activitiesRepository: ActivitiesRepository = ActivitiesRepositoryImpl(context)

    // Use cases
    override val getActivitiesUseCase: GetActivitiesUseCase = GetActivitiesUseCase(activitiesRepository)
    override val newActivityUseCase: NewActivityUseCase = NewActivityUseCase(activitiesRepository)
    override val deleteActivityUseCase: DeleteActivityUseCase = DeleteActivityUseCase(activitiesRepository)
    override val addNoteToLastActivityUseCase: AddNoteToLastActivityUseCase = AddNoteToLastActivityUseCase(activitiesRepository)
    override val saveActivitiesUseCase: SaveActivitiesUseCase = SaveActivitiesUseCase(activitiesRepository)
    override val updateActivityUseCase: UpdateActivityUseCase = UpdateActivityUseCase(activitiesRepository)
}

fun createAndroidAppContainer(context: Context): AndroidAppContainer =
    AndroidAppContainer(context)

actual fun createAppContainer(): AppContainer {
    error("On Android, use createAndroidAppContainer with Context instead")
}