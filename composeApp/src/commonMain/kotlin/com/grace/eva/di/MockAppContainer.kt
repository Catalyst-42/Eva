package com.grace.eva.di

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository
import com.grace.eva.domain.repository.MockActivitiesRepository
import com.grace.eva.domain.usecase.ActivitiesExportUseCase
import com.grace.eva.domain.usecase.AddNoteToLastActivityUseCase
import com.grace.eva.domain.usecase.DeleteActivityUseCase
import com.grace.eva.domain.usecase.GetActivitiesUseCase
import com.grace.eva.domain.usecase.NewActivityUseCase
import com.grace.eva.domain.usecase.SaveActivitiesUseCase
import com.grace.eva.domain.usecase.UpdateActivityUseCase
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
    private val type: MockType = MockType.EMPTY
) : AppContainer {
    private val mockRepo = MockActivitiesRepository()

    // Factory to create test data based on type
    private fun createMockActivities(): Activities = when (type) {
        MockType.EMPTY -> Activities(
            name = "Новое сохранение", activities = mutableListOf()
        )

        MockType.SIMPLE -> Activities(
            name = "Тестовое сохранение", activities = mutableListOf(
                Activity(
                    name = "Единственная активность",
                    note = "Простая заметка",
                    begin = Clock.System.now() - 30.minutes,
                    end = null
                )
            )
        )

        MockType.LARGE -> Activities(
            name = "Большой проект", activities = mutableListOf(
                Activity(
                    "Планирование",
                    "Обсудили задачи",
                    Clock.System.now() - 5.hours,
                    Clock.System.now() - 4.hours - 32.minutes - 16.seconds
                ), Activity(
                    "Разработка",
                    "Пишем код",
                    Clock.System.now() - 4.hours - 32.minutes - 16.seconds,
                    Clock.System.now() - 2.hours
                ), Activity(
                    "Тестирование",
                    "Проверяем баги",
                    Clock.System.now() - 2.hours - 32.minutes - 16.seconds,
                    Clock.System.now() - 1.hours
                ), Activity(
                    "Релиз",
                    "Выпускаем версию",
                    Clock.System.now() - 1.hours,
                    null
                )
            )
        )
    }

    private val mockActivities = createMockActivities()

    // Generic use case that does nothing
    private inner class MockUseCase(private val repo: ActivitiesRepository) {
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T> asUseCase(): T = when (T::class) {
            GetActivitiesUseCase::class -> object : GetActivitiesUseCase(repo) {
                override fun invoke(): Flow<Activities> = flowOf(mockActivities)
            } as T

            NewActivityUseCase::class -> object : NewActivityUseCase(repo) {
                override suspend fun invoke(name: String, note: String) {}
            } as T

            DeleteActivityUseCase::class -> object : DeleteActivityUseCase(repo) {
                override suspend fun invoke(activity: Activity) {}
            } as T

            AddNoteToLastActivityUseCase::class -> object : AddNoteToLastActivityUseCase(repo) {
                override suspend fun invoke(note: String) {}
            } as T

            SaveActivitiesUseCase::class -> object : SaveActivitiesUseCase(repo) {
                override suspend fun invoke() {}
            } as T

            UpdateActivityUseCase::class -> object : UpdateActivityUseCase(repo) {
                override suspend fun invoke(activity: Activity) {}
            } as T

            ActivitiesExportUseCase::class -> object : ActivitiesExportUseCase(repo) {
                override suspend fun invoke(activities: Activities) {}
            } as T

            else -> error("Unknown use case")
        }
    }

    private val mockUseCase = MockUseCase(mockRepo)

    override val getActivitiesUseCase: GetActivitiesUseCase = mockUseCase.asUseCase()
    override val newActivityUseCase: NewActivityUseCase = mockUseCase.asUseCase()
    override val deleteActivityUseCase: DeleteActivityUseCase = mockUseCase.asUseCase()
    override val addNoteToLastActivityUseCase: AddNoteToLastActivityUseCase =
        mockUseCase.asUseCase()
    override val saveActivitiesUseCase: SaveActivitiesUseCase = mockUseCase.asUseCase()
    override val updateActivityUseCase: UpdateActivityUseCase = mockUseCase.asUseCase()
    override val activitiesExportUseCase: ActivitiesExportUseCase = mockUseCase.asUseCase()
}