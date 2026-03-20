package com.grace.eva.domain.model

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ActivityTemplate @OptIn(ExperimentalUuidApi::class) constructor(
    val name: String,
    val color: String = "#2196F3",
    val id: String = Uuid.random().toString()
)
