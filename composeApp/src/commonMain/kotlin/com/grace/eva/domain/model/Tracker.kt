package com.grace.eva.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tracker(
    val saves: List<String> = emptyList(),
    val currentSaveId: String? = null,
)