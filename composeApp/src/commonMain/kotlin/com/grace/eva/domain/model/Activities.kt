package com.grace.eva.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Activities @OptIn(ExperimentalUuidApi::class) constructor(
    val name: String = "",
    val begin: Instant = Clock.System.now(),
    val activities: MutableList<Activity> = mutableListOf(),
    val end: Instant? = null,
    val id: String = Uuid.random().toString(),
)
