package com.grace.eva.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Formats input instant to readable string.
 *
 * @param instant The instant to format
 * @param timeZone Time zone to use (defaults to system)
 * @return Formatted string in "dd.MM HH:mm:ss" format
 */
fun formatTime(
    instant: Instant,
    format: String = "dd.mm.yyyy HH:MM:SS",
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val dateTime = instant.toLocalDateTime(timeZone)

    return format
        .replace("dd", dateTime.day.pad())
        .replace("mm", dateTime.month.number.pad())
        .replace("yyyy", dateTime.year.pad(4))
        .replace("HH", dateTime.hour.pad())
        .replace("MM", dateTime.minute.pad())
        .replace("SS", dateTime.second.pad())
}

private fun Int.pad(length: Int = 2): String = toString().padStart(length, '0')
