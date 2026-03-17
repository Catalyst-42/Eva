package com.grace.eva.di

import android.content.Context
import com.grace.eva.data.repository.ActivitiesRepositoryImpl
import com.grace.eva.domain.repository.ActivitiesRepository
import com.grace.eva.domain.usecase.ActivitiesExportUseCase
import com.grace.eva.domain.usecase.ActivityRemoveUseCase
import com.grace.eva.domain.usecase.ActivitiesGetUseCase
import com.grace.eva.domain.usecase.ActivityNewUseCase
import com.grace.eva.domain.usecase.ActivitiesSaveUseCase
import com.grace.eva.domain.usecase.ActivityUpdateUseCase

/**
 * DI class
 *
 * Used to define all use cases to be used lately in
 * view functions
 */
class AndroidAppContainer(context: Context): AppContainer {
    private val activitiesRepository: ActivitiesRepository = ActivitiesRepositoryImpl(context)

    // Use cases
    override val activitiesGetUseCase: ActivitiesGetUseCase = ActivitiesGetUseCase(activitiesRepository)
    override val activityNewUseCase: ActivityNewUseCase = ActivityNewUseCase(activitiesRepository)
    override val activityRemoveUseCase: ActivityRemoveUseCase = ActivityRemoveUseCase(activitiesRepository)
    override val activitiesSaveUseCase: ActivitiesSaveUseCase = ActivitiesSaveUseCase(activitiesRepository)
    override val activityUpdateUseCase: ActivityUpdateUseCase = ActivityUpdateUseCase(activitiesRepository)
    override val activitiesExportUseCase: ActivitiesExportUseCase = ActivitiesExportUseCase(activitiesRepository)
}

fun createAndroidAppContainer(context: Context): AndroidAppContainer =
    AndroidAppContainer(context)

actual fun createAppContainer(): AppContainer {
    error("On Android, use createAndroidAppContainer with Context instead")
}