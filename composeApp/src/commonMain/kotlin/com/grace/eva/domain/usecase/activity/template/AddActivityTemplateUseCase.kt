package com.grace.eva.domain.usecase.activity.template

import com.grace.eva.domain.repository.TrackerRepository

open class AddActivityTemplateUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke(name: String, color: String) {
        repository.addActivityTemplate(name, color)
    }
}