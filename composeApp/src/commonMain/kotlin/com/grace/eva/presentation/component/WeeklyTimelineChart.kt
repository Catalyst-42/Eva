package com.grace.eva.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grace.eva.domain.model.Activity
import com.grace.eva.utils.formatDuration
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import kotlin.collections.forEachIndexed
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

data class TimeSegment(
    val startHour: Float,
    val endHour: Float,
    val activityName: String,
    val color: Color,
    val activity: Activity,
    val duration: Long,
    val isActive: Boolean = false
)

data class DayTimeline(
    val date: LocalDate,
    val segments: List<TimeSegment>
)

@Composable
fun ActivitiesMapChart(
    activities: List<Activity>,
    isSaveCompleted: Boolean,
    saveEnd: Instant? = null,
    getColorForActivity: (String) -> Color,
    modifier: Modifier = Modifier
) {
    if (activities.isEmpty()) return

    val timeZone = TimeZone.currentSystemDefault()

    // Current time that updates only at midnight
    var currentTime by remember { mutableStateOf(kotlin.time.Clock.System.now()) }

    // Wait until next midnight, then update
    LaunchedEffect(Unit) {
        while (true) {
            val now = kotlin.time.Clock.System.now()
            val nowLocal = now.toLocalDateTime(timeZone)
            val nextMidnight = nowLocal.date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
            val millisUntilMidnight = (nextMidnight - now).inWholeMilliseconds
            kotlinx.coroutines.delay(millisUntilMidnight)
            currentTime = kotlin.time.Clock.System.now()
        }
    }

    // First activity date adjusted to start of week (Monday)
    val firstActivityRawDate = activities.first().begin.toLocalDateTime(timeZone).date
    val firstActivityDate = remember(firstActivityRawDate) {
        val daysFromMonday = firstActivityRawDate.dayOfWeek.ordinal
        firstActivityRawDate.minus(daysFromMonday, DateTimeUnit.DAY)
    }

    // Last date to show (today if save is active, otherwise save end)
    val lastDate = if (isSaveCompleted && saveEnd != null) {
        saveEnd.toLocalDateTime(timeZone).date
    } else {
        currentTime.toLocalDateTime(timeZone).date
    }

    // Total number of weeks from first activity Monday to last date
    val totalWeeks = remember(firstActivityDate, lastDate) {
        var daysDiff = 0
        var date = firstActivityDate
        while (date <= lastDate) {
            daysDiff++
            date = date.plus(1, DateTimeUnit.DAY)
        }
        ((daysDiff - 1) / 7) + 1
    }

    var currentWeekOffset by rememberSaveable { mutableStateOf(totalWeeks - 1) }

    // Reset to last week when save completes
    LaunchedEffect(isSaveCompleted, saveEnd) {
        if (isSaveCompleted && saveEnd != null) {
            currentWeekOffset = totalWeeks - 1
        }
    }

    // Keep offset within bounds
    LaunchedEffect(totalWeeks) {
        if (currentWeekOffset > totalWeeks - 1) {
            currentWeekOffset = totalWeeks - 1
        }
    }

    // Days of the current week
    val weekDays = remember(currentWeekOffset, firstActivityDate) {
        val startOfWeek = firstActivityDate.plus(currentWeekOffset * 7, DateTimeUnit.DAY)
        (0..6).map { offset ->
            startOfWeek.plus(offset, DateTimeUnit.DAY)
        }
    }

    // Build timelines for each day of the week
    val daysTimeline = remember(activities, currentTime, isSaveCompleted, saveEnd, weekDays, timeZone) {
        buildWeekDaysTimeline(
            activities = activities,
            currentTime = currentTime,
            isSaveCompleted = isSaveCompleted,
            saveEnd = saveEnd,
            getColorForActivity = getColorForActivity,
            weekDays = weekDays,
            timeZone = timeZone
        )
    }

    val barHeight = 220.dp

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Карта недели",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentWeekOffset > 0) currentWeekOffset-- },
                    enabled = currentWeekOffset > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Предыдущая неделя",
                        modifier = Modifier.size(20.dp),
                        tint = if (currentWeekOffset > 0)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Text(
                    text = "${currentWeekOffset + 1} из $totalWeeks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = { if (currentWeekOffset < totalWeeks - 1) currentWeekOffset++ },
                    enabled = currentWeekOffset < totalWeeks - 1,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Следующая неделя",
                        modifier = Modifier.size(20.dp),
                        tint = if (currentWeekOffset < totalWeeks - 1)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysTimeline.forEachIndexed { index, day ->
                key(day.date) {
                    WeekDayBar(
                        dayTimeline = day,
                        barHeight = barHeight,
                        hasActiveSegment = day.segments.any { it.isActive },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekDayBar(
    dayTimeline: DayTimeline,
    barHeight: Dp,
    hasActiveSegment: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val outlineColor = Color.Black
    val backgroundColor = MaterialTheme.colorScheme.surface
    val timeZone = TimeZone.currentSystemDefault()

    var currentTime by remember { mutableStateOf(kotlin.time.Clock.System.now()) }

    // Update every second only if this bar has an active segment
    LaunchedEffect(hasActiveSegment) {
        if (hasActiveSegment) {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                currentTime = kotlin.time.Clock.System.now()
            }
        }
    }

    // Build segments with dynamic update for active segment
    val timelineData = remember(dayTimeline, currentTime, hasActiveSegment) {
        if (hasActiveSegment && dayTimeline.segments.any { it.isActive }) {
            val segments = dayTimeline.segments.toMutableList()
            val activeIndex = segments.indexOfFirst { it.isActive }

            if (activeIndex != -1) {
                val activeSegment = segments[activeIndex]
                val now = currentTime.toLocalDateTime(timeZone)
                val currentHour = now.hour + now.minute / 60f + now.second / 3600f
                val newEndHour = currentHour.coerceAtMost(24f)

                // Update only if new end hour > start hour (avoid zero-length)
                if (newEndHour > activeSegment.startHour) {
                    val updatedSegment = activeSegment.copy(
                        endHour = newEndHour,
                        duration = ((newEndHour - activeSegment.startHour) * 3600).toLong()
                    )
                    segments[activeIndex] = updatedSegment
                    dayTimeline.copy(segments = segments)
                } else {
                    dayTimeline
                }
            } else {
                dayTimeline
            }
        } else {
            dayTimeline
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            val barBottomY = size.height
            val hours = listOf(0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24)

            // Draw grid lines
            hours.forEach { hour ->
                val y = barBottomY * (1 - (hour / 24f))

                val isOverlapping = if (timelineData.segments.isNotEmpty()) {
                    val firstSegment = timelineData.segments.first()
                    val lastSegment = timelineData.segments.last()

                    val firstStartY = barBottomY * (1 - (firstSegment.startHour / 24f))
                    val firstEndY = barBottomY * (1 - (firstSegment.endHour / 24f))
                    val lastStartY = barBottomY * (1 - (lastSegment.startHour / 24f))
                    val lastEndY = barBottomY * (1 - (lastSegment.endHour / 24f))

                    val firstTop = minOf(firstStartY, firstEndY)
                    val firstBottom = maxOf(firstStartY, firstEndY)
                    val lastTop = minOf(lastStartY, lastEndY)
                    val lastBottom = maxOf(lastStartY, lastEndY)

                    y in (firstTop - 1.dp.toPx())..(firstBottom + 1.dp.toPx()) ||
                            y in (lastTop - 1.dp.toPx())..(lastBottom + 1.dp.toPx())
                } else {
                    false
                }

                if (!isOverlapping) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
            }

            // Draw hour labels
            hours.forEach { hour ->
                val y = barBottomY * (1 - (hour / 24f))

                val isOverlapping = if (timelineData.segments.isNotEmpty()) {
                    val firstSegment = timelineData.segments.first()
                    val lastSegment = timelineData.segments.last()

                    val firstStartY = barBottomY * (1 - (firstSegment.startHour / 24f))
                    val firstEndY = barBottomY * (1 - (firstSegment.endHour / 24f))
                    val lastStartY = barBottomY * (1 - (lastSegment.startHour / 24f))
                    val lastEndY = barBottomY * (1 - (lastSegment.endHour / 24f))

                    val firstTop = minOf(firstStartY, firstEndY)
                    val firstBottom = maxOf(firstStartY, firstEndY)
                    val lastTop = minOf(lastStartY, lastEndY)
                    val lastBottom = maxOf(lastStartY, lastEndY)

                    y in (firstTop - 1.dp.toPx())..(firstBottom + 1.dp.toPx()) ||
                            y in (lastTop - 1.dp.toPx())..(lastBottom + 1.dp.toPx())
                } else {
                    false
                }

                if (!isOverlapping) {
                    drawIntoCanvas { canvas ->
                        val text = "${hour}"
                        val textLayoutResult = textMeasurer.measure(
                            text = text,
                            style = TextStyle(
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center,
                                color = gridColor
                            )
                        )

                        val centerX = size.width / 2
                        val centerY = y - (textLayoutResult.size.height / 2)
                        val left = centerX - textLayoutResult.size.width / 2 - 2.dp.toPx()
                        val top = centerY - 1.dp.toPx()
                        val right = centerX + textLayoutResult.size.width / 2 + 2.dp.toPx()
                        val bottom = centerY + textLayoutResult.size.height + 1.dp.toPx()

                        canvas.nativeCanvas.apply {
                            drawRect(
                                color = backgroundColor,
                                topLeft = Offset(left, top),
                                size = Size(right - left, bottom - top)
                            )
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(centerX - textLayoutResult.size.width / 2, centerY),
                                color = gridColor
                            )
                        }
                    }
                }
            }

            // Draw segments (bars)
            timelineData.segments.forEach { segment ->
                val startY = barBottomY * (1 - (segment.startHour / 24f))
                val endY = barBottomY * (1 - (segment.endHour / 24f))
                val height = startY - endY

                if (height > 0) {
                    drawRect(
                        color = segment.color,
                        topLeft = Offset(0f, endY),
                        size = Size(size.width, height)
                    )
                }
            }

            // Draw segment outlines
            timelineData.segments.forEach { segment ->
                val startY = barBottomY * (1 - (segment.startHour / 24f))
                val endY = barBottomY * (1 - (segment.endHour / 24f))
                val height = startY - endY

                if (height > 0) {
                    drawRect(
                        color = outlineColor,
                        topLeft = Offset(0f, endY),
                        size = Size(size.width, height),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // Draw duration text for long segments
            timelineData.segments.forEach { segment ->
                val startY = barBottomY * (1 - (segment.startHour / 24f))
                val endY = barBottomY * (1 - (segment.endHour / 24f))
                val height = startY - endY
                val durationHours = segment.duration / 3600f

                if (height > 0 && durationHours >= 1.5) {
                    val duration = segment.duration.toDuration(DurationUnit.SECONDS)
                    val text = formatDuration(duration)

                    drawIntoCanvas { canvas ->
                        val textLayoutResult = textMeasurer.measure(
                            text = text,
                            style = TextStyle(
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                        )

                        val centerX = size.width / 2
                        val centerY = endY + (height / 2) - (textLayoutResult.size.height / 2)

                        canvas.nativeCanvas.apply {
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(centerX - textLayoutResult.size.width / 2, centerY),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = formatDate(timelineData.date),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun buildWeekDaysTimeline(
    activities: List<Activity>,
    currentTime: Instant,
    isSaveCompleted: Boolean,
    saveEnd: Instant?,
    getColorForActivity: (String) -> Color,
    weekDays: List<LocalDate>,
    timeZone: TimeZone
): List<DayTimeline> {
    val relevantActivities = activities.filter { activity ->
        val endTime = when {
            activity == activities.last() && !isSaveCompleted -> currentTime
            activity == activities.last() && isSaveCompleted && saveEnd != null -> saveEnd
            else -> activities[activities.indexOf(activity) + 1].begin
        }

        val activityStartDate = activity.begin.toLocalDateTime(timeZone).date
        val activityEndDate = endTime.toLocalDateTime(timeZone).date

        activityEndDate >= weekDays.first() && activityStartDate <= weekDays.last()
    }

    if (relevantActivities.isEmpty()) return weekDays.map { DayTimeline(it, emptyList()) }

    val dayMap = weekDays.associateWith { mutableListOf<TimeSegment>() }.toMutableMap()

    relevantActivities.forEachIndexed { index, activity ->
        val isLastActivity = index == relevantActivities.lastIndex
        val isActiveSegment = isLastActivity && !isSaveCompleted

        val endTime = when {
            index < relevantActivities.lastIndex -> relevantActivities[index + 1].begin
            !isSaveCompleted -> currentTime
            isSaveCompleted && saveEnd != null -> saveEnd
            else -> currentTime
        }

        var currentStart = activity.begin

        while (currentStart < endTime) {
            val startDateTime = currentStart.toLocalDateTime(timeZone)
            val currentDate = startDateTime.date

            if (currentDate !in weekDays.first()..weekDays.last()) {
                if (currentDate > weekDays.last()) break
                currentStart = currentDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
                continue
            }

            val midnight = currentDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
            val startHour = startDateTime.hour + startDateTime.minute / 60f + startDateTime.second / 3600f

            if (endTime <= midnight) {
                // Last day of this activity
                val endDateTime = endTime.toLocalDateTime(timeZone)
                var endHour = endDateTime.hour + endDateTime.minute / 60f + endDateTime.second / 3600f

                // If endHour is 0 (midnight), it means the activity ends exactly at midnight
                // We should show a full day segment (to 24) or a zero-length segment?
                // For midnight end, the segment should go to 24 (end of day)
                if (endHour == 0f && endDateTime.date == currentDate.plus(1, DateTimeUnit.DAY)) {
                    endHour = 24f
                }

                val duration = ((endHour - startHour) * 3600).toLong()

                dayMap[currentDate]?.add(
                    TimeSegment(
                        startHour = startHour,
                        endHour = endHour,
                        activityName = activity.name,
                        color = getColorForActivity(activity.name),
                        activity = activity,
                        duration = duration,
                        isActive = isActiveSegment && currentDate == endDateTime.date
                    )
                )
                break
            } else {
                // Full day segment (activity continues to next day)
                val endHour = 24f
                val duration = ((endHour - startHour) * 3600).toLong()

                dayMap[currentDate]?.add(
                    TimeSegment(
                        startHour = startHour,
                        endHour = endHour,
                        activityName = activity.name,
                        color = getColorForActivity(activity.name),
                        activity = activity,
                        duration = duration,
                        isActive = false
                    )
                )
                currentStart = midnight
            }
        }
    }

    return weekDays.map { date ->
        DayTimeline(
            date = date,
            segments = dayMap[date]?.sortedBy { it.startHour } ?: emptyList()
        )
    }
}

private fun formatDate(date: LocalDate): String {
    return "${date.day.toString().padStart(2, '0')}.${date.month.number.toString().padStart(2, '0')}"
}

@Preview(showBackground = true)
@Composable
private fun PreviewActivitiesMapChart() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val mockActivities = listOf(
                Activity(name = "Сон", begin = System.now().minus(5.days).minus(22.hours)),
                Activity(name = "Работа", begin = System.now().minus(4.days)),
                Activity(name = "Отдых", begin = System.now().minus(3.days).minus(5.hours)),
                Activity(name = "Работа", begin = System.now().minus(1.days).minus(12.hours)),
            )

            fun getColor(name: String): Color = when (name) {
                "Работа" -> Color(0xFF2196F3)
                "Отдых" -> Color(0xFFFF9800)
                "Сон" -> Color(0xFF4CAF50)
                else -> Color(0xFF9C27B0)
            }

            ActivitiesMapChart(
                activities = mockActivities,
                isSaveCompleted = false,
                saveEnd = null,
                getColorForActivity = { name -> getColor(name) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}