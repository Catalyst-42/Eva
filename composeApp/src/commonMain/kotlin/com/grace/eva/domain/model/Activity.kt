package com.grace.eva.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Activity(
    var name: String,
    var note: String = "",
    var begin: Instant = Clock.System.now(),
) {
    @OptIn(ExperimentalUuidApi::class)
    @Transient
    val id: String = Uuid.random().toString()
}