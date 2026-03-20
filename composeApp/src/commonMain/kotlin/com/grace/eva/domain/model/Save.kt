package com.grace.eva.domain.model

import com.grace.eva.ui.theme.tracker.TemplateColors
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Save @OptIn(ExperimentalUuidApi::class) constructor(
    val name: String = "Новое сохранение",
    val activities: MutableList<Activity> = mutableListOf(),
    val activityTemplates: MutableList<ActivityTemplate> = mutableListOf(
        ActivityTemplate("Активность", TemplateColors.getColorForIndex(0)),
    ),
    val end: Instant? = null,
    val id: String = Uuid.random().toString(),
)
