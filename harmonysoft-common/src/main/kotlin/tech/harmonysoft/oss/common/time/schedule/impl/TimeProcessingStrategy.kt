package tech.harmonysoft.oss.common.time.schedule.impl

import java.time.DayOfWeek

interface TimeProcessingStrategy<T> {

    fun toDayOfWeek(time: T): DayOfWeek

    fun timeMsFromStartOfTheDay(time: T): Long

    fun timeMsBeforeEndOfTheDay(time: T): Long
}