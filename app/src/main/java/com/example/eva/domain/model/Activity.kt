package com.example.eva.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Activity(
    var name: String,
    var note: String = "",
    var begin: Instant = Clock.System.now(),
    var end: Instant? = null,
)
