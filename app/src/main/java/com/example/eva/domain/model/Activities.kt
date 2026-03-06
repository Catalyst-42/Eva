package com.example.eva.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Activities(
    val begin: Instant = Clock.System.now(),
    val activities: MutableList<Activity> = mutableListOf()
)
