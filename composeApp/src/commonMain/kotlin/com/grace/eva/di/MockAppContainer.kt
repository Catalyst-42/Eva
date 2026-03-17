package com.grace.eva.di

import com.grace.eva.domain.model.Save
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.MockTrackerRepository
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class MockType {
    EMPTY, SIMPLE, LARGE
}

class MockAppContainer(
    private val type: MockType = MockType.EMPTY,
) : AppContainer {

    override val trackerRepository: TrackerRepository = MockTrackerRepository()

    private val mockSave = when (type) {
        MockType.EMPTY -> Save(name = "Новое сохранение")
        MockType.SIMPLE -> Save(
            name = "Тестовое сохранение",
            activities = mutableListOf(
                Activity("Единственная активность", "Простая заметка", Clock.System.now() - 30.minutes)
            )
        )
        MockType.LARGE -> Save(
            name = "Большой проект",
            activities = mutableListOf(
                Activity("Планирование", "Обсудили задачи", Clock.System.now() - 5.hours - 32.minutes - 16.seconds),
                Activity("Разработка", "Пишем код", Clock.System.now() - 4.hours - 32.minutes - 16.seconds),
                Activity("Тестирование", "Проверяем баги", Clock.System.now() - 2.hours - 32.minutes - 16.seconds),
                Activity("Релиз", "Выпускаем версию", Clock.System.now() - 1.hours - 32.minutes)
            )
        )
    }

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

    override val createActivityUseCase = object : CreateActivityUseCase(trackerRepository) {
        override suspend fun invoke(name: String) {}
    }

    override val removeActivityUseCase = object : RemoveActivityUseCase(trackerRepository) {
        override suspend fun invoke(activity: Activity) {}
    }

    override val updateActivityUseCase = object : UpdateActivityUseCase(trackerRepository) {
        override suspend fun invoke(activity: Activity) {}
    }

    override val exportSaveUseCase = object : ExportSaveUseCase(trackerRepository) {
        override suspend fun invoke(save: Save) {}
    }

    override val importSaveUseCase = object : ImportSaveUseCase(trackerRepository) {
        override suspend fun invoke() {}
    }
}