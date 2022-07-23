package tech.harmonysoft.oss.common.time.schedule.impl

import tech.harmonysoft.oss.common.time.clock.ClockProvider
import tech.harmonysoft.oss.common.time.util.TimeUtil.Millis
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

class TimeMillisProcessingStrategy(
    clockProvider: ClockProvider
) : TimeProcessingStrategy<Long> {

    /**
     * Holds anchor time in millis for the beginning of last Monday.
     *
     * We can't use the first Monday of year 1970 because, for example, in Singapore time offset was shifted
     * by 30 minutes in 1933. That's why in theory we might have wrong processing for some historical data.
     * But we assume that we work with current time and applications restart often enough to keep this field
     * updated
     */
    private val lastMondayStartTimeMillis: Long

    /**
     * We might have a situation when during application restart time there was DST but now DST is ended,
     * or vice versa. Then the calculation of [timeMsFromStartOfTheDay] and [timeMsBeforeEndOfTheDay] might
     * be inaccurate. So, we record DST offset for the anchor last Monday and use it for time calculation.
     */
    private val lastMondayStartTimeDstOffset: Int

    private val zoneId = clockProvider.data.zone
    private val timeZone = TimeZone.getTimeZone(zoneId)
    private val useDst = timeZone.useDaylightTime()
    private val calendarInstance = Calendar.getInstance(timeZone)

    init {
        val currentTimeMillis = clockProvider.data.millis()
        val beginningOfToday = ZonedDateTime
            .ofInstant(Instant.ofEpochMilli(currentTimeMillis), zoneId)
            .toLocalDate()
            .atStartOfDay(zoneId)

        lastMondayStartTimeMillis = beginningOfToday
            .minusDays(beginningOfToday.dayOfWeek.ordinal.toLong())
            .toInstant()
            .toEpochMilli()

        lastMondayStartTimeDstOffset = getDstOffset(lastMondayStartTimeMillis)
    }

    override fun toDayOfWeek(time: Long): DayOfWeek {
        val anchorMondayStartMillisBeforeTime = anchorMondayStartMillisBeforeTime(time)
        val dayShift = (time - anchorMondayStartMillisBeforeTime) / Millis.DAY
        return DayOfWeek.MONDAY + dayShift
    }

    /**
     * @param time      target point of time in millis
     * @return          time in millis for the Monday happened before the given time
     */
    private fun anchorMondayStartMillisBeforeTime(time: Long): Long {
        if (lastMondayStartTimeMillis <= time) {
            return lastMondayStartTimeMillis
        }

        val weeksToRollback = (lastMondayStartTimeMillis - time) / Millis.WEEK + 1
        val base = lastMondayStartTimeMillis - weeksToRollback * Millis.WEEK

        return if (useDst) {
            base + lastMondayStartTimeDstOffset - getDstOffset(time)
        } else {
            base
        }
    }

    private fun getDstOffset(time: Long): Int {
        return synchronized(calendarInstance) {
            calendarInstance.timeInMillis = time
            calendarInstance.get(Calendar.DST_OFFSET)
        }
    }

    override fun timeMsFromStartOfTheDay(time: Long): Long {
        val anchorMondayStartTimeMillis = anchorMondayStartMillisBeforeTime(time)
        val dstOffsetShift = if (useDst) {
            val timeDstOffset = getDstOffset(time)
            timeDstOffset - if (anchorMondayStartTimeMillis == lastMondayStartTimeMillis) {
                lastMondayStartTimeDstOffset
            } else {
                getDstOffset(anchorMondayStartTimeMillis)
            }
        } else {
            0
        }
        return (time + dstOffsetShift - anchorMondayStartTimeMillis) % Millis.DAY
    }

    override fun timeMsBeforeEndOfTheDay(time: Long): Long {
        return Millis.DAY - timeMsFromStartOfTheDay(time)
    }
}