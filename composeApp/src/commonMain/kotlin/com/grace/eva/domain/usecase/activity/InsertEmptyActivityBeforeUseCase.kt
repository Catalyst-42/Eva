package com.grace.eva.domain.usecase.activity

import com.grace.eva.domain.model.Activity
import com.grace.eva.domain.repository.TrackerRepository

open class InsertEmptyActivityBeforeUseCase(
    private val repository: TrackerRepository
) {
    open suspend operator fun invoke(activity: Activity) {
        repository.insertEmptyActivityBefore(activity)
    }
}