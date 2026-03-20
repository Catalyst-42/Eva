package com.grace.eva.domain.usecase.activity.template

import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.domain.repository.TrackerRepository

open class UpdateActivityTemplateUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke(template: ActivityTemplate) {
        repository.updateActivityTemplate(template)
    }
}
