package com.grace.eva.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Activities @OptIn(ExperimentalUuidApi::class) constructor(
    val name: String = "Новое сохранение",
    val activities: MutableList<Activity> = mutableListOf(),
    val end: Instant? = null,
    val id: String = Uuid.random().toString(),
)
