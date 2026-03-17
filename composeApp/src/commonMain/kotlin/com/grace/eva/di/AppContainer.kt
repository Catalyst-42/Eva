package com.grace.eva.di

import com.grace.eva.domain.repository.TrackerRepository
import com.grace.eva.domain.usecase.activity.CreateActivityUseCase
import com.grace.eva.domain.usecase.activity.RemoveActivityUseCase
import com.grace.eva.domain.usecase.activity.UpdateActivityUseCase
import com.grace.eva.domain.usecase.sync.ExportSaveUseCase
import com.grace.eva.domain.usecase.save.CreateSaveUseCase
import com.grace.eva.domain.usecase.save.DeleteSaveUseCase
import com.grace.eva.domain.usecase.save.GetAllSavesUseCase
import com.grace.eva.domain.usecase.save.GetCurrentSaveUseCase
import com.grace.eva.domain.usecase.save.SetCurrentSaveUseCase
import com.grace.eva.domain.usecase.save.UpdateSaveUseCase
import com.grace.eva.domain.usecase.sync.ImportSaveUseCase

interface AppContainer {
    val trackerRepository: TrackerRepository

    // Save UseCases
    val getAllSavesUseCase: GetAllSavesUseCase
    val getCurrentSaveUseCase: GetCurrentSaveUseCase
    val setCurrentSaveUseCase: SetCurrentSaveUseCase
    val createSaveUseCase: CreateSaveUseCase
    val deleteSaveUseCase: DeleteSaveUseCase
    val updateSaveUseCase: UpdateSaveUseCase

    // Activity UseCases
    val createActivityUseCase: CreateActivityUseCase
    val removeActivityUseCase: RemoveActivityUseCase
    val updateActivityUseCase: UpdateActivityUseCase

    // Sync
    val exportSaveUseCase: ExportSaveUseCase
    val importSaveUseCase: ImportSaveUseCase
}

expect fun createAppContainer(): AppContainer