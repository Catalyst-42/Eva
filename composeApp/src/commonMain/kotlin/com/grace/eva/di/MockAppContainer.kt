package com.grace.eva.di

import com.grace.eva.domain.model.Save
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.domain.repository.MockTrackerRepository
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
import com.grace.eva.ui.theme.tracker.TemplateColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

enum class MockType {
    EMPTY, SIMPLE, LARGE
}

class MockAppContainer(
    type: MockType = MockType.EMPTY,
) : AppContainer {
    override val trackerRepository: TrackerRepository = MockTrackerRepository()

    private val now = Clock.System.now()

    private val mockSave = when (type) {
        MockType.EMPTY -> Save(
            name = "Пустое сохранение",
            activities = mutableListOf(),
            activityTemplates = mutableListOf()
        )

        MockType.SIMPLE -> {
            val startTime = now - 7.days
            val activitiesList = listOf(
                "Сон" to 8.hours,
                "Работа" to 8.hours,
                "Обед" to 1.hours,
                "Работа" to 4.hours,
                "Спорт" to 1.5.hours,
                "Ужин" to 1.hours,
                "Отдых" to 2.hours
            )

            val activities = mutableListOf<Activity>()
            var currentTime = startTime

            repeat(5) { day ->
                activitiesList.forEach { (name, duration) ->
                    activities.add(Activity(name, "$name activity", currentTime))
                    currentTime += duration
                }
            }

            Save(
                name = "Средняя неделя",
                activities = activities,
                activityTemplates = mutableListOf(
                    ActivityTemplate("Сон", TemplateColors.getColorForIndex(0)),
                    ActivityTemplate("Работа", TemplateColors.getColorForIndex(1)),
                    ActivityTemplate("Обед", TemplateColors.getColorForIndex(2)),
                    ActivityTemplate("Спорт", TemplateColors.getColorForIndex(3)),
                    ActivityTemplate("Ужин", TemplateColors.getColorForIndex(4)),
                    ActivityTemplate("Отдых", TemplateColors.getColorForIndex(5))
                )
            )
        }

        MockType.LARGE -> {
            val startTime = now - 18.days

            Save(
                name = "Многоделье",
                activities = mutableListOf<Activity>().apply {
                    val activities = listOf(
                        "Сон" to 8.hours,
                        "Завтрак" to 1.hours,
                        "Работа" to 8.hours,
                        "Обед" to 1.hours,
                        "Работа" to 4.hours,
                        "Спорт" to 1.5.hours,
                        "Ужин" to 1.hours,
                        "Отдых" to 2.hours,
                        "Обучение" to 1.5.hours,
                        "Сон" to 8.hours
                    )

                    var currentTime = startTime

                    repeat(17) {
                        activities.forEach { (name, duration) ->
                            add(Activity(name, "$name activity", currentTime))
                            currentTime += duration
                        }

                        currentTime += 30.minutes
                    }
                },
                activityTemplates = mutableListOf(
                    ActivityTemplate("Сон", TemplateColors.getColorForIndex(0)),
                    ActivityTemplate("Завтрак", TemplateColors.getColorForIndex(1)),
                    ActivityTemplate("Работа", TemplateColors.getColorForIndex(2)),
                    ActivityTemplate("Обед", TemplateColors.getColorForIndex(3)),
                    ActivityTemplate("Спорт", TemplateColors.getColorForIndex(4)),
                    ActivityTemplate("Ужин", TemplateColors.getColorForIndex(5)),
                    ActivityTemplate("Отдых", TemplateColors.getColorForIndex(6)),
                    ActivityTemplate("Обучение", TemplateColors.getColorForIndex(7)),
                    ActivityTemplate("Прогулка", TemplateColors.getColorForIndex(8)),
                    ActivityTemplate("Встречи", TemplateColors.getColorForIndex(9)),
                    ActivityTemplate("Документация", TemplateColors.getColorForIndex(11)),
                    ActivityTemplate("Код-ревью", TemplateColors.getColorForIndex(12)),
                    ActivityTemplate("Планерка", TemplateColors.getColorForIndex(13)),
                    ActivityTemplate("Перерыв", TemplateColors.getColorForIndex(14))
                )
            )
        }
    }

    // Save UseCases
    override val getAllSavesUseCase = object : GetAllSavesUseCase(trackerRepository) {
        override suspend fun invoke(): Flow<List<Save>> = flowOf(listOf(mockSave))
    }

    override val getCurrentSaveUseCase = object : GetCurrentSaveUseCase(trackerRepository) {
        override suspend fun invoke(): Flow<Save?> = flowOf(mockSave)
    }

    override val setCurrentSaveUseCase = object : SetCurrentSaveUseCase(trackerRepository) {
        override suspend fun invoke(save: Save) {}
    }

    override val createSaveUseCase = object : CreateSaveUseCase(trackerRepository) {
        override suspend fun invoke(name: String): Save = Save(name = name)
    }

    override val deleteSaveUseCase = object : DeleteSaveUseCase(trackerRepository) {
        override suspend fun invoke(save: Save) {}
    }

    override val updateSaveUseCase = object : UpdateSaveUseCase(trackerRepository) {
        override suspend fun invoke(save: Save) {}
    }

    // Activity UseCases
    override val createActivityUseCase = object : CreateActivityUseCase(trackerRepository) {
        override suspend fun invoke(name: String) {}
    }

    override val removeActivityUseCase = object : RemoveActivityUseCase(trackerRepository) {
        override suspend fun invoke(activity: Activity) {}
    }

    override val updateActivityUseCase = object : UpdateActivityUseCase(trackerRepository) {
        override suspend fun invoke(activity: Activity) {}
    }

    // ActivityTemplate UseCases
    override val addActivityTemplateUseCase = object : AddActivityTemplateUseCase(trackerRepository) {
        override suspend fun invoke(name: String, color: String) {}
    }

    override val removeActivityTemplateUseCase = object : RemoveActivityTemplateUseCase(trackerRepository) {
        override suspend fun invoke(template: ActivityTemplate) {}
    }

    override val updateActivityTemplateUseCase = object : UpdateActivityTemplateUseCase(trackerRepository) {
        override suspend fun invoke(template: ActivityTemplate) {}
    }

    override val getActivityTemplatesUseCase = object : GetActivityTemplatesUseCase(trackerRepository) {
        override suspend fun invoke(): Flow<List<ActivityTemplate>> {
            return flowOf(mockSave.activityTemplates)
        }
    }

    // Sync UseCases
    override val exportSaveUseCase = object : ExportSaveUseCase(trackerRepository) {
        override suspend fun invoke(save: Save) {}
    }

    override val importSaveUseCase = object : ImportSaveUseCase(trackerRepository) {
        override suspend fun invoke() {}
    }
}