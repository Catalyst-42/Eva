package com.grace.eva.utils

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
    val totalSeconds = duration.inWholeSeconds
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> hours.toString() + ":" +
                minutes.toString().padStart(2, '0') + ":" +
                seconds.toString().padStart(2, '0')
        minutes > 0 -> minutes.toString() + ":" +
                seconds.toString().padStart(2, '0')
        else -> seconds.toString() + "с"
    }
}
