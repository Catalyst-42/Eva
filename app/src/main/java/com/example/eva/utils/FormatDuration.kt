package com.example.eva.utils

import kotlin.time.Duration

/**
 * Formats input duration in human-readable form.
 *
 * @param duration The duration to format
 * @return Formatted string:
 *   - For durations less than 1 minute: "Xс"
 *   - For durations less than 1 hour: "MM:SS"
 *   - For durations 1 hour or more: "HH:MM:SS"
 */
fun formatDuration(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60
    val seconds = duration.inWholeSeconds % 60
    return when {
        hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
        minutes > 0 -> "%d:%02d".format(minutes, seconds)
        else -> "%dс".format(seconds)
    }
}
