package com.grace.eva.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

/**
 * Parses a date-time string in format "dd.MM.yyyy HH:mm:ss"to Instant using kotlinx-datetime
 * @param dateTimeString String in format "dd.MM.yyyy HH:mm:ss" (e.g., "16.04.2004 14:30:45")
 * @return Instant representing the parsed date-time, or null if parsing fails
 */
fun String.toInstant(dateTimeString: String): Instant? {
    return try {
        val parts = split(" ", ".")
        if (parts.size != 6) return null

        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()
        val hour = parts[3].toInt()
        val minute = parts[4].toInt()
        val second = parts[5].toInt()

        val localDateTime = LocalDateTime(
            year = year, month = month, day = day, hour = hour, minute = minute, second = second
        )

        localDateTime.toInstant(TimeZone.currentSystemDefault())
    } catch (e: Exception) {
        null
    }
}
