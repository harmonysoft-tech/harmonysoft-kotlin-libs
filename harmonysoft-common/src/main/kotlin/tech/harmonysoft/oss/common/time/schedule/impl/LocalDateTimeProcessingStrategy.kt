package tech.harmonysoft.oss.common.time.schedule.impl

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

object LocalDateTimeProcessingStrategy : TimeProcessingStrategy<LocalDateTime> {

    override fun toDayOfWeek(time: LocalDateTime): DayOfWeek {
        return time.dayOfWeek
    }

    override fun timeMsFromStartOfTheDay(time: LocalDateTime): Long {
        return timeMsFromStartOfTheDay(time.toLocalTime())
    }

    fun timeMsFromStartOfTheDay(time: LocalTime): Long {
        return Duration.between(LocalTime.MIN, time).toMillis()
    }

    override fun timeMsBeforeEndOfTheDay(time: LocalDateTime): Long {
        // '+1' because LocalTime.MAX is 23:59:59.(9)
        return Duration.between(time.toLocalTime(), LocalTime.MAX).toMillis() + 1L
    }
}