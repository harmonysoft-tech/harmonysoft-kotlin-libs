package tech.harmonysoft.oss.common.time.schedule.impl

import tech.harmonysoft.oss.common.collection.CollectionInitializer
import tech.harmonysoft.oss.common.time.clock.ClockProvider
import tech.harmonysoft.oss.common.time.schedule.Schedule
import tech.harmonysoft.oss.common.time.util.TimeUtil.Millis
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

data class ScheduleImpl(
    val clockProvider: ClockProvider,
    val dayTimeWindows: Collection<DaysOfWeekTimeWindows>
) : Schedule {

    private val timeMillisStrategy = TimeMillisProcessingStrategy(clockProvider)

    private val byDayOfWeek = dayTimeWindows.fold(
        mutableMapOf<DayOfWeek, MutableList<DaysOfWeekTimeWindows>>()
    ) { byDayOfWeek, daysTimeWindow ->
        byDayOfWeek.apply {
            for (dayOfWeek in daysTimeWindow.daysOfWeek) {
                val days = byDayOfWeek.getOrPut(dayOfWeek, CollectionInitializer.mutableList())
                days += daysTimeWindow
            }
        }
    }.mapValues { it.value.toTypedArray() }.toMap()

    init {
        if (dayTimeWindows.isEmpty()) {
            throw IllegalArgumentException("time windows should not be empty")
        }
        validateOverlapping()
    }

    private fun validateOverlapping() {
        for ((dayOfWeek, daysTimeWindows) in byDayOfWeek) {
            for (i in daysTimeWindows.indices) {
                for (j in i + 1 until daysTimeWindows.size) {
                    val d1 = daysTimeWindows[i]
                    val d2 = daysTimeWindows[j]
                    if (overlaps(d1, d2)) {
                        throw IllegalArgumentException(
                            "detected overlapping schedule setup for $dayOfWeek in '$d1' and '$d2'"
                        )
                    }
                }
            }
        }
    }

    private fun overlaps(d1: DaysOfWeekTimeWindows, d2: DaysOfWeekTimeWindows): Boolean {
        return d1.timeWindows.any { w1 ->
            d2.timeWindows.any { w2 ->
                w1.overlaps(w2)
            }
        }
    }

    override fun timeMsUntilTargetTimeIntervalStart(dateTime: LocalDateTime): Long {
        return timeMsUntilTargetTimeIntervalStart(dateTime, LocalDateTimeProcessingStrategy)
    }

    override fun timeMsUntilTargetTimeIntervalStart(timeMillis: Long): Long {
        return timeMsUntilTargetTimeIntervalStart(timeMillis, timeMillisStrategy)
    }

    private fun <T> timeMsUntilTargetTimeIntervalStart(time: T, strategy: TimeProcessingStrategy<T>): Long {
        val startDayOfWeek = strategy.toDayOfWeek(time)
        var todayProcessed = false
        var timeShiftMillis = 0L
        var dayOfWeek = startDayOfWeek
        while (!todayProcessed || startDayOfWeek != dayOfWeek) {
            if (dayOfWeek == startDayOfWeek) {
                todayProcessed = true
            }
            timeShiftMillis += when {
                startDayOfWeek == dayOfWeek -> 0L
                startDayOfWeek + 1 == dayOfWeek -> strategy.timeMsBeforeEndOfTheDay(time)
                else -> Millis.DAY
            }
            val daysTimeWindows = byDayOfWeek[dayOfWeek]
            if (daysTimeWindows == null) {
                dayOfWeek += 1
                continue
            }
            val dayTimeOffsetMillis = if (dayOfWeek == startDayOfWeek) {
                strategy.timeMsFromStartOfTheDay(time)
            } else {
                0L
            }
            val result = timeMsUntilTargetTimeIntervalStart(dayTimeOffsetMillis, daysTimeWindows)
            when {
                result == 0L -> return if (timeShiftMillis <= 0L) {
                    -1L
                } else {
                    timeShiftMillis
                }

                result > 0L -> return result + timeShiftMillis
            }
            dayOfWeek += 1
        }

        // There is a possible case that there is a single time window (e.g. 10:00 - 11:00) and given point of time
        // is after it (e.g. 12:00). That way we want to return time till the end of the day (12 hours) plus
        // 6 days plus time before the time window start (10 hours)
        val daysTimeWindows = byDayOfWeek[startDayOfWeek]
        if (daysTimeWindows != null) {
            var result = -1L
            for (dayTimeWindows in daysTimeWindows) {
                val timeMs = dayTimeWindows.timeMsUntilTargetTimeIntervalStart(0L)
                if (timeMs < 0L) {
                    continue
                }
                if (result < 0L || result > timeMs) {
                    result = timeMs
                }
            }
            if (result >= 0) {
                // remaining time on the target day
                return strategy.timeMsBeforeEndOfTheDay(time) +
                       // roll to the next week
                       SIX_DAYS_MILLIS +
                       result
            }
        }
        throw IllegalStateException(
            "can't calculate time from '$time' until target interval start in schedule $this"
        )
    }

    private fun timeMsUntilTargetTimeIntervalStart(
        dayTimeOffsetMillis: Long,
        daysTimeWindows: Array<DaysOfWeekTimeWindows>
    ): Long {
        var result = -1L
        for (dayTimeWindow in daysTimeWindows) {
            val timeMs = dayTimeWindow.timeMsUntilTargetTimeIntervalStart(dayTimeOffsetMillis)
            if (timeMs < 0L) {
                continue
            }
            if (timeMs == 0L) {
                return 0
            }
            if (result < 0L || result > timeMs) {
                result = timeMs
            }
        }
        return result
    }

    override fun timeMsUntilTargetTimeIntervalEnd(dateTime: LocalDateTime): Long {
        return timeMsUntilTargetTimeIntervalEnd(dateTime, LocalDateTimeProcessingStrategy)
    }

    override fun timeMsUntilTargetTimeIntervalEnd(timeMillis: Long): Long {
        return timeMsUntilTargetTimeIntervalEnd(timeMillis, timeMillisStrategy)
    }

    private fun <T> timeMsUntilTargetTimeIntervalEnd(time: T, strategy: TimeProcessingStrategy<T>): Long {
        val timeWindows = byDayOfWeek[strategy.toDayOfWeek(time)] ?: return -1L
        val dayTimeOffsetMillis = strategy.timeMsFromStartOfTheDay(time)
        for (timeWindow in timeWindows) {
            val result = timeWindow.timeMsUntilTargetTimeIntervalEnd(dayTimeOffsetMillis)
            if (result >= 0L) {
                return result
            }
        }
        return -1L
    }

    companion object {
        private val SIX_DAYS_MILLIS = TimeUnit.DAYS.toMillis(6)
    }
}