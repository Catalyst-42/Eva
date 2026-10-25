package com.grace.eva.util

import kotlin.time.Duration

/**
 * Formats input duration in human-readable form.
 *
 * @param duration The duration to format
 * @return Formatted string:
 *   - For durations less than 1 minute: "SSс" or "-SSс"
 *   - For durations less than 1 hour: "MM:SS" or "-MM:SS"
 *   - For durations less than 1 day: "HH:MM:SS" or "-HH:MM:SS"
 *   - For durations 1 day or more: "X д HH:MM:SS" or "-X д HH:MM:SS"
 *   - For durations exactly X days: "X д" or "-X д"
 */
fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val isNegative = totalSeconds < 0
    val absoluteSeconds = kotlin.math.abs(totalSeconds)
    
    val days = absoluteSeconds / (3600 * 24)
    val hours = (absoluteSeconds % (3600 * 24)) / 3600
    val minutes = (absoluteSeconds % 3600) / 60
    val seconds = absoluteSeconds % 60

    val signPrefix = if (isNegative) "-" else ""

    return when {
        days > 0 -> {
            if (hours == 0L && minutes == 0L && seconds == 0L) {
                "$signPrefix$days д"
            } else {
                "$signPrefix$days д ${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
            }
        }
        hours > 0 -> "$signPrefix${hours}:${minutes.pad()}:${seconds.pad()}"
        minutes > 0 -> "$signPrefix${minutes}:${seconds.pad()}"
        else -> "$signPrefix${seconds}с"
    }
}

private fun Long.pad(): String = toString().padStart(2, '0')