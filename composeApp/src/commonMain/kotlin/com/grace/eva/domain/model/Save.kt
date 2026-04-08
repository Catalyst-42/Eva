package com.grace.eva.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Save @OptIn(ExperimentalUuidApi::class) constructor(
    val name: String = "Новое сохранение",
    val activities: MutableList<Activity> = mutableListOf(),
    val activityTemplates: MutableList<ActivityTemplate> = mutableListOf(),
    val end: Instant? = null,
    val updatedAt: Instant = Clock.System.now(),
    val id: String = Uuid.random().toString(),
)