package tech.harmonysoft.oss.common.time.schedule.impl

import java.time.DayOfWeek

data class DaysOfWeekTimeWindows(
    val daysOfWeek: Set<DayOfWeek>,
    private val _timeWindows: Set<TimeWindow>
) {

    val timeWindows = _timeWindows.toTypedArray()

    init {
        for (timeWindow in timeWindows) {
            timeWindows.find {
                it != timeWindow && it.overlaps(timeWindow)
            }?.let { overlappingTimeWindow ->
                throw IllegalArgumentException(
                    "detected overlapping time windows: '$timeWindow' and '$overlappingTimeWindow' in $this"
                )
            }
        }
    }

    /**
     * @param dayTimeOffsetMillis       number of milliseconds elapsed from the beginning of the day,
     *                                  think about this as [LocalTime] raw representation
     * @return  a positive value if there is a [configured time window][timeWindows] which contains
     *          the given point of time. Returned value contains a number of milliseconds beofre the
     *          time window end;
     *
     *          non-positive value as an indication that given point of time doesn't belong to any
     *          configured time window
     */
    fun timeMsUntilTargetTimeIntervalEnd(dayTimeOffsetMillis: Long): Long {
        return timeWindows.find {
            it.inWindow(dayTimeOffsetMillis)
        }?.let {
            it.endTimeDayOffsetMillis - dayTimeOffsetMillis
        } ?: -1L
    }

    /**
     * @return  a positive value if there is a [configured time window][timeWindows] which starts after the given
     *          point of time. Returned value contains a number of milliseconds before that time window;
     *
     *          zero as an indication that there is a configured time window which contains given point of time;
     *
     *          negative value as an indication that there is no time window which contains given point of time
     *          or starts after it
     */
    fun timeMsUntilTargetTimeIntervalStart(dayTimeOffestMillis: Long): Long {
        var result = -1L
        for (timeWindow in timeWindows) {
            if (timeWindow.inWindow(dayTimeOffestMillis)) {
                return 0
            }
            if (timeWindow.startTimeDayOffsetMillis > dayTimeOffestMillis) {
                val timeMs = timeWindow.startTimeDayOffsetMillis - dayTimeOffestMillis
                if (result < 0L || result > timeMs) {
                    result = timeMs
                }
            }
        }
        return result
    }
}