package com.grace.eva.di

import com.grace.eva.domain.usecase.ActivitiesExportUseCase
import com.grace.eva.domain.usecase.ActivityRemoveUseCase
import com.grace.eva.domain.usecase.ActivitiesGetUseCase
import com.grace.eva.domain.usecase.ActivityNewUseCase
import com.grace.eva.domain.usecase.ActivitiesSaveUseCase
import com.grace.eva.domain.usecase.ActivityUpdateUseCase

interface AppContainer {
    val activitiesGetUseCase: ActivitiesGetUseCase
    val activityNewUseCase: ActivityNewUseCase
    val activityRemoveUseCase: ActivityRemoveUseCase
    val activitiesSaveUseCase: ActivitiesSaveUseCase
    val activityUpdateUseCase: ActivityUpdateUseCase
    val activitiesExportUseCase: ActivitiesExportUseCase
}

expect fun createAppContainer(): AppContainer