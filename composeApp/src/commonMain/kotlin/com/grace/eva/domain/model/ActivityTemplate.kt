package com.grace.eva.domain.model

import com.grace.eva.ui.theme.tracker.TemplateColors
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@ConsistentCopyVisibility
data class ActivityTemplate private constructor(
    val name: String = "Активность",
    val color: String = TemplateColors.getColorForIndex(0),
    val isHidden: Boolean = false,
    @Transient private val _id: String? = null
) {
    @OptIn(ExperimentalUuidApi::class)
    val id: String = _id ?: Uuid.random().toString()

    constructor(
        name: String = "Активность",
        color: String = TemplateColors.getColorForIndex(0),
        isHidden: Boolean = false
    ) : this(name, color, isHidden, null)

    fun copy(
        name: String = this.name,
        color: String = this.color,
        isHidden: Boolean = this.isHidden
    ): ActivityTemplate {
        return ActivityTemplate(
            name = name,
            color = color,
            isHidden = isHidden,
            _id = this.id
        )
    }
}