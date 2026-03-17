package com.grace.eva.domain.usecase

import com.grace.eva.domain.model.Activities
import com.grace.eva.domain.repository.ActivitiesRepository

open class ActivitiesExportUseCase(
    private val repository: ActivitiesRepository
) {
    open suspend operator fun invoke(activities: Activities) {
        repository.activitiesExport(activities)
    }
}