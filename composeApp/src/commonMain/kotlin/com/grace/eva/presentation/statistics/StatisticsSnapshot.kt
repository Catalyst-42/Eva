package com.grace.eva.presentation.statistics

import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.domain.model.Save
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.TimeZone
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

data class ActivityStat(
    val name: String,
    val totalDuration: Duration,
    val count: Int,
    val minDuration: Duration,
    val maxDuration: Duration,
    val firstOccurrenceIndex: Int
)

data class StatisticsSnapshot(
    val saveId: String,
    val activityStats: List<ActivityStat>,
    val totalDuration: Duration,
    val totalActivities: Int,
    val totalDays: Double,
    val weeklyStatistics: List<WeeklyStatistics>
)

object StatisticsSnapshotCache {
    private val cachedSnapshot = MutableStateFlow<StatisticsSnapshot?>(null)

    fun get(saveId: String): StatisticsSnapshot? =
        cachedSnapshot.value?.takeIf { it.saveId == saveId }

    fun put(snapshot: StatisticsSnapshot) {
        cachedSnapshot.value = snapshot
    }
}

fun createStatisticsSnapshot(
    save: Save,
    activityTemplates: List<ActivityTemplate>,
    updatedAt: Instant,
    timeZone: TimeZone
): StatisticsSnapshot {
    val activities = save.activities.sortedBy { it.begin }
    val endTime = save.end ?: updatedAt
    val hiddenNames = activityTemplates.filter { it.isHidden }.map { it.name }.toSet()
    val durationsByName = linkedMapOf<String, MutableList<Duration>>()
    val firstOccurrenceByName = mutableMapOf<String, Int>()

    activities.forEachIndexed { index, activity ->
        val activityEnd = activities.getOrNull(index + 1)?.begin ?: endTime
        val duration = activityEnd - activity.begin
        if (duration > Duration.ZERO && activity.name !in hiddenNames) {
            durationsByName.getOrPut(activity.name) { mutableListOf() }.add(duration)
            if (activity.name !in firstOccurrenceByName) {
                firstOccurrenceByName[activity.name] = index
            }
        }
    }

    val activityStats = durationsByName.map { (name, durations) ->
        ActivityStat(
            name = name,
            totalDuration = durations.sumOf { it.inWholeSeconds }.seconds,
            count = durations.size,
            minDuration = durations.minOrNull() ?: Duration.ZERO,
            maxDuration = durations.maxOrNull() ?: Duration.ZERO,
            firstOccurrenceIndex = firstOccurrenceByName[name] ?: Int.MAX_VALUE
        )
    }.sortedBy { it.firstOccurrenceIndex }

    val totalDuration = activityStats.sumOf { it.totalDuration.inWholeSeconds }.seconds
    val totalDays = activities.firstOrNull()?.let { firstActivity ->
        val elapsedSeconds = (endTime - firstActivity.begin).inWholeSeconds.toDouble()
        if (elapsedSeconds > 0) elapsedSeconds / 86400.0 else 1.0
    } ?: 1.0

    val weeklyStatistics = calculateWeeklyStatistics(
        activities = activities,
        endTime = endTime,
        timeZone = timeZone,
        activityOrder = activityTemplates.map { it.name }
    )

    return StatisticsSnapshot(
        saveId = save.id,
        activityStats = activityStats,
        totalDuration = totalDuration,
        totalActivities = activityStats.sumOf { it.count },
        totalDays = totalDays,
        weeklyStatistics = weeklyStatistics
    )
}