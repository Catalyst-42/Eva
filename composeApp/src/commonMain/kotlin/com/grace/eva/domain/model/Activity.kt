package com.grace.eva.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@ConsistentCopyVisibility
data class Activity private constructor(
    var name: String = "Активность",
    var note: String = "",
    var begin: Instant = Clock.System.now(),
    @Transient private val _id: String? = null
) {
    @OptIn(ExperimentalUuidApi::class)
    val id: String = _id ?: Uuid.random().toString()

    constructor(
        name: String = "Активность",
        note: String = "",
        begin: Instant = Clock.System.now()
    ) : this(name, note, begin, null)

    fun copy(
        name: String = this.name,
        note: String = this.note,
        begin: Instant = this.begin
    ): Activity {
        return Activity(
            name = name,
            note = note,
            begin = begin,
            _id = this.id
        )
    }
}