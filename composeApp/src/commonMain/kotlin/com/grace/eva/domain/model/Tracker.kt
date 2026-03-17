package com.grace.eva.domain.model

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Tracker @OptIn(ExperimentalUuidApi::class) constructor(
    val saves: MutableList<Save> = mutableListOf(),
    val currentSaveId: String? = null,
    val id: String = Uuid.random().toString(),
)
