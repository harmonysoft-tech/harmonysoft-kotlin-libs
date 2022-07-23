package tech.harmonysoft.oss.common.time.schedule.impl

import java.time.LocalTime

data class TimeWindow(
    val startTime: LocalTime,
    val endTime: LocalTime
) {

    val startTimeDayOffsetMillis = LocalDateTimeProcessingStrategy.timeMsFromStartOfTheDay(startTime)
    val endTimeDayOffsetMillis = LocalDateTimeProcessingStrategy.timeMsFromStartOfTheDay(endTime)

    init {
        if (!startTime.isBefore(endTime)) {
            throw IllegalArgumentException(
                "detected an attempt to use a ${this::class.qualifiedName} where start time ($startTime) is "
                + "not before end time ($endTime)"
            )
        }
    }

    fun inWindow(localTime: LocalTime): Boolean {
        return !localTime.isBefore(startTime) && !localTime.isAfter(endTime)
    }

    fun inWindow(dayTimeOffsetMilis: Long): Boolean {
        return dayTimeOffsetMilis >= startTimeDayOffsetMillis && dayTimeOffsetMilis <= endTimeDayOffsetMillis
    }

    fun overlaps(other: TimeWindow): Boolean {
        return other.inWindow(startTime.plusSeconds(1)) || other.inWindow(endTime.minusSeconds(1))
    }

    companion object {
        const val END_OF_DAY = "<EOD>"
        private val TIME_RANGE_REGEX = """\s*([^-\s]+)\s*-\s*([^\\s]+)""".toRegex()

        fun parse(timeWindowString: String): TimeWindow {
            val (start, end) = TIME_RANGE_REGEX.matchEntire(timeWindowString)
                ?.destructured
                ?.let { (start, end) ->
                    try {
                        val endTime = if (end == END_OF_DAY) {
                            LocalTime.MAX
                        } else {
                            LocalTime.parse(end)
                        }
                        LocalTime.parse(start) to endTime
                    } catch (e: Exception) {
                        throw IllegalArgumentException(
                            "failed to parse time window. Start time string: '$start', end time string: '$end'", e
                        )
                    }
                } ?: throw IllegalArgumentException("failed to parse time window from '$timeWindowString'")
            return TimeWindow(start, end)
        }
    }
}