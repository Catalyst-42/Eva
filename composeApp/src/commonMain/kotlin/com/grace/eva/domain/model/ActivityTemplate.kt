package com.grace.eva.domain.model

import com.grace.eva.ui.theme.tracker.TemplateColors
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ActivityTemplate @OptIn(ExperimentalUuidApi::class) constructor(
    val name: String,
    val color: String,
    val isHidden: Boolean = false,
    val id: String = Uuid.random().toString()
)
