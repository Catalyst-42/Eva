package com.grace.eva.utils

import kotlin.time.Duration

/**
 * Formats input duration in human-readable form.
 *
 * @param duration The duration to format
 * @return Formatted string:
 *   - For durations less than 1 minute: "SSс"
 *   - For durations less than 1 hour: "MM:SS"
 *   - For durations less than 1 day: "HH:MM:SS"
 *   - For durations 1 day or more: "X д HH:MM:SS"
 *   - For durations exactly X days: "X д"
 */
fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val days = totalSeconds / (3600 * 24)
    val hours = (totalSeconds % (3600 * 24)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        days > 0 -> {
            if (hours == 0L && minutes == 0L && seconds == 0L) {
                "$days д"
            } else {
                "$days д ${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
            }
        }
        hours > 0 -> "${hours}:${minutes.pad()}:${seconds.pad()}"
        minutes > 0 -> "${minutes}:${seconds.pad()}"
        else -> "${seconds}с"
    }
}

private fun Long.pad(): String = toString().padStart(2, '0')