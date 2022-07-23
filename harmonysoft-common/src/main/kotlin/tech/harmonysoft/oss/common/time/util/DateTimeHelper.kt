package tech.harmonysoft.oss.common.time.util

import tech.harmonysoft.oss.common.time.clock.ClockProvider
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
class DateTimeHelper(
    private val clockProvider: ClockProvider
) {

    private val patterns = ConcurrentHashMap<String, DateTimeFormatter>()

    fun formatDate(date: LocalDate): String {
        return getFormatter(DATE_PATTERN).format(date)
    }

    fun formatDate(date: LocalDate, pattern: String): String {
        return getFormatter(pattern).format(date)
    }

    fun formatTime(time: LocalTime): String {
        return getFormatter(TIME_PATTERN).format(time)
    }

    fun formatTime(time: LocalTime, pattern: String): String {
        return getFormatter(pattern).format(time)
    }

    fun formatDateTime(dateTime: LocalDateTime): String {
        return getFormatter(DATE_TIME_PATTERN).format(dateTime)
    }

    fun formatDateTime(dateTime: LocalDateTime, pattern: String): String {
        return getFormatter(pattern).format(dateTime)
    }

    fun isWeekend(time: Instant, timeZone: ZoneId): Boolean {
        val dayOfWeek = DayOfWeek.from(time.atZone(timeZone))
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
    }

    fun parseTime(raw: String): LocalTime {
        return parseTime(raw, TIME_PATTERN)
    }

    fun parseTime(raw: String, pattern: String): LocalTime {
        return try {
            LocalTime.parse(raw, getFormatter(pattern))
        } catch (e: Exception) {
            throw IllegalArgumentException("failed to parse local time from '$raw' using pattern '$pattern'", e)
        }
    }

    fun parseDate(raw: String): LocalDate {
        return parseDate(raw, DATE_PATTERN)
    }

    fun parseDate(raw: String, pattern: String): LocalDate {
        return try {
            LocalDate.parse(raw, getFormatter(pattern))
        } catch (e: Exception) {
            throw IllegalArgumentException("failed to parse local date from '$raw' using pattern '$pattern'", e)
        }
    }

    fun parseDateTime(raw: String): LocalDateTime {
        return parseDateTime(raw, DATE_TIME_PATTERN)
    }

    fun parseDateTime(raw: String, pattern: String): LocalDateTime {
        return try {
            LocalDateTime.parse(raw, getFormatter(pattern))
        } catch (e: Exception) {
            throw IllegalArgumentException("failed to parse local date time from '$raw' using pattern '$pattern'", e)
        }
    }

    fun getFormatter(pattern: String): DateTimeFormatter {
        return patterns[pattern] ?: patterns.computeIfAbsent(pattern) {
            DateTimeFormatter.ofPattern(pattern)
        }
    }

    companion object {
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val TIME_PATTERN = "HH:mm:ss.SSS"
        const val DATE_TIME_PATTERN = "$DATE_PATTERN $TIME_PATTERN"
    }
}