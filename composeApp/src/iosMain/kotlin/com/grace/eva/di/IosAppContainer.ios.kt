package com.grace.eva.di

import com.grace.eva.data.repository.ActivitiesRepositoryImpl
import com.grace.eva.domain.repository.ActivitiesRepository
import com.grace.eva.domain.usecase.ActivitiesExportUseCase
import com.grace.eva.domain.usecase.ActivityRemoveUseCase
import com.grace.eva.domain.usecase.ActivitiesGetUseCase
import com.grace.eva.domain.usecase.ActivityNewUseCase
import com.grace.eva.domain.usecase.ActivitiesSaveUseCase
import com.grace.eva.domain.usecase.ActivityUpdateUseCase

class IosAppContainer() : AppContainer {
    private val activitiesRepository: ActivitiesRepository =
        ActivitiesRepositoryImpl()

    override val activitiesGetUseCase: ActivitiesGetUseCase =
        ActivitiesGetUseCase(activitiesRepository)
    override val activityNewUseCase: ActivityNewUseCase =
        ActivityNewUseCase(activitiesRepository)
    override val activityRemoveUseCase: ActivityRemoveUseCase =
        ActivityRemoveUseCase(activitiesRepository)
    override val activitiesSaveUseCase: ActivitiesSaveUseCase =
        ActivitiesSaveUseCase(activitiesRepository)
    override val activityUpdateUseCase: ActivityUpdateUseCase =
        ActivityUpdateUseCase(activitiesRepository)
    override val activitiesExportUseCase: ActivitiesExportUseCase =
        ActivitiesExportUseCase(activitiesRepository)
}

actual fun createAppContainer(): AppContainer = IosAppContainer()
