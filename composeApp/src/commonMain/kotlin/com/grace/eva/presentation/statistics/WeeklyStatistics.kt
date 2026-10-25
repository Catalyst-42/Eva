package com.grace.eva.presentation.statistics

import com.grace.eva.domain.model.Activity
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

data class WeeklyActivityDuration(
    val activityName: String,
    val duration: Duration
)

data class WeeklyStatistics(
    val weekStart: LocalDate,
    val totalDuration: Duration,
    val weekDuration: Duration,
    val activityDurations: List<WeeklyActivityDuration>
)

fun calculateWeeklyStatistics(
    activities: List<Activity>,
    endTime: Instant,
    timeZone: TimeZone,
    activityOrder: List<String> = emptyList()
): List<WeeklyStatistics> {
    if (activities.isEmpty()) return emptyList()

    val orderedActivities = activities.sortedBy { it.begin }
    val firstDate = orderedActivities.first().begin.toLocalDateTime(timeZone).date
    var weekStart = firstDate.minus(firstDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
    val weekStarts = mutableListOf<LocalDate>()
    while (weekStart.atStartOfDayIn(timeZone) < endTime) {
        weekStarts += weekStart
        weekStart = weekStart.plus(7, DateTimeUnit.DAY)
    }

    val durationsByWeek = weekStarts.associateWith { mutableMapOf<String, Long>() }
    val totalDurationsByWeek = weekStarts.associateWith { mutableMapOf<String, Long>() }

    orderedActivities.forEachIndexed { index, activity ->
        val intervalStart = activity.begin
        val intervalEnd = orderedActivities.getOrNull(index + 1)?.begin ?: endTime
        if (intervalEnd <= intervalStart) return@forEachIndexed

        weekStarts.forEach { startDate ->
            val weekStartInstant = startDate.atStartOfDayIn(timeZone)
            val weekEndInstant = startDate.plus(7, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
            val clippedStart = maxOf(intervalStart, weekStartInstant)
            val clippedEnd = minOf(intervalEnd, weekEndInstant)
            val seconds = (clippedEnd - clippedStart).inWholeSeconds
            if (seconds > 0) {
                val weekDurations = durationsByWeek.getValue(startDate)
                weekDurations[activity.name] = (weekDurations[activity.name] ?: 0L) + seconds
                totalDurationsByWeek[startDate]!![activity.name] =
                    (totalDurationsByWeek[startDate]!![activity.name] ?: 0L) + seconds
            }
        }
    }

    val activityOrderIndex = activityOrder.withIndex().associate { it.value to it.index }

    return weekStarts.map { startDate ->
        val weekStartInstant = startDate.atStartOfDayIn(timeZone)
        val weekEndInstant = startDate.plus(7, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
        val durations = durationsByWeek.getValue(startDate)
            .map { (name, seconds) -> WeeklyActivityDuration(name, seconds.seconds) }
            .sortedWith(
                compareBy<WeeklyActivityDuration> {
                    activityOrderIndex[it.activityName] ?: Int.MAX_VALUE
                }
                    .thenBy { it.activityName }
            )
        val totalDuration = totalDurationsByWeek[startDate]!!.values.sum().seconds
        WeeklyStatistics(
            weekStart = startDate,
            totalDuration = totalDuration,
            weekDuration = weekEndInstant - weekStartInstant,
            activityDurations = durations
        )
    }
}