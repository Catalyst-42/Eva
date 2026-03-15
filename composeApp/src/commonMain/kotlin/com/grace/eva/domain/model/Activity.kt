package com.grace.eva.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Activity @OptIn(ExperimentalUuidApi::class) constructor(
    var name: String,
    var note: String = "",
    var begin: Instant = Clock.System.now(),
    val id: String = Uuid.random().toString(),
)
