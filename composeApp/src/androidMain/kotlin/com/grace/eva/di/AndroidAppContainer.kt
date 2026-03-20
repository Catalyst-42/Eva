package com.grace.eva.di

import android.content.Context
import com.grace.eva.data.repository.TrackerRepositoryImpl
import com.grace.eva.domain.repository.TrackerRepository
import com.grace.eva.domain.usecase.activity.CreateActivityUseCase
import com.grace.eva.domain.usecase.activity.RemoveActivityUseCase
import com.grace.eva.domain.usecase.activity.UpdateActivityUseCase
import com.grace.eva.domain.usecase.activity.template.AddActivityTemplateUseCase
import com.grace.eva.domain.usecase.activity.template.GetActivityTemplatesUseCase
import com.grace.eva.domain.usecase.activity.template.RemoveActivityTemplateUseCase
import com.grace.eva.domain.usecase.activity.template.UpdateActivityTemplateUseCase
import com.grace.eva.domain.usecase.sync.ExportSaveUseCase
import com.grace.eva.domain.usecase.save.CreateSaveUseCase
import com.grace.eva.domain.usecase.save.DeleteSaveUseCase
import com.grace.eva.domain.usecase.save.GetAllSavesUseCase
import com.grace.eva.domain.usecase.save.GetCurrentSaveUseCase
import com.grace.eva.domain.usecase.save.SetCurrentSaveUseCase
import com.grace.eva.domain.usecase.save.UpdateSaveUseCase
import com.grace.eva.domain.usecase.sync.ImportSaveUseCase

class AndroidAppContainer(context: Context) : AppContainer {

    override val trackerRepository: TrackerRepository = TrackerRepositoryImpl(context)

    // Save UseCases
    override val getAllSavesUseCase: GetAllSavesUseCase = GetAllSavesUseCase(trackerRepository)
    override val getCurrentSaveUseCase: GetCurrentSaveUseCase = GetCurrentSaveUseCase(trackerRepository)
    override val setCurrentSaveUseCase: SetCurrentSaveUseCase = SetCurrentSaveUseCase(trackerRepository)
    override val createSaveUseCase: CreateSaveUseCase = CreateSaveUseCase(trackerRepository)
    override val deleteSaveUseCase: DeleteSaveUseCase = DeleteSaveUseCase(trackerRepository)
    override val updateSaveUseCase: UpdateSaveUseCase = UpdateSaveUseCase(trackerRepository)

    // Activity UseCases
    override val createActivityUseCase: CreateActivityUseCase = CreateActivityUseCase(trackerRepository)
    override val removeActivityUseCase: RemoveActivityUseCase = RemoveActivityUseCase(trackerRepository)
    override val updateActivityUseCase: UpdateActivityUseCase = UpdateActivityUseCase(trackerRepository)

    // ActivityTemplate UseCases
    override val addActivityTemplateUseCase: AddActivityTemplateUseCase = AddActivityTemplateUseCase(trackerRepository)
    override val removeActivityTemplateUseCase: RemoveActivityTemplateUseCase = RemoveActivityTemplateUseCase(trackerRepository)
    override val updateActivityTemplateUseCase: UpdateActivityTemplateUseCase = UpdateActivityTemplateUseCase(trackerRepository)
    override val getActivityTemplatesUseCase: GetActivityTemplatesUseCase = GetActivityTemplatesUseCase(trackerRepository)

    // Export
    override val exportSaveUseCase: ExportSaveUseCase = ExportSaveUseCase(trackerRepository)
    override val importSaveUseCase: ImportSaveUseCase = ImportSaveUseCase(trackerRepository)
}

fun createAndroidAppContainer(context: Context): AndroidAppContainer = AndroidAppContainer(context)

actual fun createAppContainer(): AppContainer {
    error("On Android, use createAndroidAppContainer with Context instead")
}