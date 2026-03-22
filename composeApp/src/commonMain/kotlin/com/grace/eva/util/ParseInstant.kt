package com.grace.eva.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

/**
 * Parses string to Instant using specified format.
 *
 * @param timeString The string to parse
 * @param format The format of the string (must match formatTime pattern)
 * @param timeZone Time zone to use (defaults to system)
 * @return Parsed Instant or null if string cannot be parsed
 */
fun parseInstant(
    timeString: String,
    format: String = "dd.mm.yyyy HH:MM:SS",
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant? {
    // Create a map of format parts to their regex patterns
    val formatParts = mapOf(
        "dd" to """\d{2}""",
        "mm" to """\d{2}""",
        "yyyy" to """\d{4}""",
        "HH" to """\d{2}""",
        "MM" to """\d{2}""",
        "SS" to """\d{2}"""
    )

    // Build regex pattern from format
    var pattern = format
    formatParts.forEach { (key, value) ->
        pattern = pattern.replace(key, "($value)")
    }

    val regex = Regex("^$pattern$")
    val matchResult = regex.matchEntire(timeString) ?: return null

    // Extract values
    val values = matchResult.groupValues.drop(1) // drop full match

    // Map values to their positions
    val formatTokens = format.split(Regex("""[.\s:]"""))
    val valueIterator = values.iterator()

    var day: Int? = null
    var month: Int? = null
    var year: Int? = null
    var hour: Int? = null
    var minute: Int? = null
    var second: Int? = null

    formatTokens.forEach { token ->
        val value = valueIterator.next().toInt()
        when (token) {
            "dd" -> day = value
            "mm" -> month = value
            "yyyy" -> year = value
            "HH" -> hour = value
            "MM" -> minute = value
            "SS" -> second = value
        }
    }

    // All date components must be present for a valid Instant
    val requiredFields = listOf(day, month, year)
    if (requiredFields.any { it == null }) {
        return null
    }

    // Use 0 as default for time components if missing
    val localDateTime = LocalDateTime(
        year = year!!,
        month = month!!,
        day = day!!,
        hour = hour ?: 0,
        minute = minute ?: 0,
        second = second ?: 0,
        nanosecond = 0
    )

    return try {
        localDateTime.toInstant(timeZone)
    } catch (e: IllegalArgumentException) {
        null // Invalid date
    }
}