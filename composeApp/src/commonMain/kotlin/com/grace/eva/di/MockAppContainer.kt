package com.grace.eva.di

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.ActivitiesRepository
import com.grace.eva.domain.repository.MockActivitiesRepository
import com.grace.eva.domain.usecase.ActivitiesExportUseCase
import com.grace.eva.domain.usecase.ActivityRemoveUseCase
import com.grace.eva.domain.usecase.ActivitiesGetUseCase
import com.grace.eva.domain.usecase.ActivityNewUseCase
import com.grace.eva.domain.usecase.ActivitiesSaveUseCase
import com.grace.eva.domain.usecase.ActivityUpdateUseCase
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
                )
            )
        )

        MockType.LARGE -> Activities(
            name = "Большой проект", activities = mutableListOf(
                Activity(
                    "Планирование",
                    "Обсудили задачи",
                    Clock.System.now() - 5.hours - 32.minutes - 16.seconds,
                ), Activity(
                    "Разработка",
                    "Пишем код",
                    Clock.System.now() - 4.hours - 32.minutes - 16.seconds,
                ), Activity(
                    "Тестирование",
                    "Проверяем баги",
                    Clock.System.now() - 2.hours - 32.minutes - 16.seconds,
                ), Activity(
                    "Релиз",
                    "Выпускаем версию",
                    Clock.System.now() - 1.hours - 32.minutes,
                )
            )
        )
    }

    private val mockActivities = createMockActivities()

    // Generic use case that does nothing
    private inner class MockUseCase(private val repo: ActivitiesRepository) {
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T> asUseCase(): T = when (T::class) {
            ActivitiesGetUseCase::class -> object : ActivitiesGetUseCase(repo) {
                override fun invoke(): Flow<Activities> = flowOf(mockActivities)
            } as T

            ActivityNewUseCase::class -> object : ActivityNewUseCase(repo) {
                override suspend fun invoke(name: String, note: String) {}
            } as T

            ActivityRemoveUseCase::class -> object : ActivityRemoveUseCase(repo) {
                override suspend fun invoke(activity: Activity) {}
            } as T

            ActivitiesSaveUseCase::class -> object : ActivitiesSaveUseCase(repo) {
                override suspend fun invoke() {}
            } as T

            ActivityUpdateUseCase::class -> object : ActivityUpdateUseCase(repo) {
                override suspend fun invoke(activity: Activity) {}
            } as T

            ActivitiesExportUseCase::class -> object : ActivitiesExportUseCase(repo) {
                override suspend fun invoke(activities: Activities) {}
            } as T

            else -> error("Unknown use case")
        }
    }

    private val mockUseCase = MockUseCase(mockRepo)

    override val activitiesGetUseCase: ActivitiesGetUseCase = mockUseCase.asUseCase()
    override val activityNewUseCase: ActivityNewUseCase = mockUseCase.asUseCase()
    override val activityRemoveUseCase: ActivityRemoveUseCase = mockUseCase.asUseCase()
    override val activitiesSaveUseCase: ActivitiesSaveUseCase = mockUseCase.asUseCase()
    override val activityUpdateUseCase: ActivityUpdateUseCase = mockUseCase.asUseCase()
    override val activitiesExportUseCase: ActivitiesExportUseCase = mockUseCase.asUseCase()
}