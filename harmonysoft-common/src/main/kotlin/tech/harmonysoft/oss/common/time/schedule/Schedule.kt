package tech.harmonysoft.oss.common.time.schedule

import java.time.LocalDateTime

interface Schedule {

    /**
     * Allows to answer if current schedule contains given point of time
     */
    fun contains(dateTime: LocalDateTime): Boolean {
        return timeMsUntilTargetTimeIntervalEnd(dateTime) >= 0L
    }

    fun contains(timeMillis: Long): Boolean {
        val timeMsUntilTargetTimeIntervalEnd = timeMsUntilTargetTimeIntervalEnd(timeMillis)
        return timeMsUntilTargetTimeIntervalEnd >= 0L
    }

    /**
     * @return  non-positive value as an indication that given point of time is already covered by the current
     *          schedule; a positive value of milliseconds remaining for the closest time window defined
     *          by the current schedule
     */
    fun timeMsUntilTargetTimeIntervalStart(dateTime: LocalDateTime): Long

    /**
     * @return  non-positive value as an indication that given point of time is already covered by the current
     *          schedule; a positive value of milliseconds remaining for the closest time window defined
     *          by the current schedule
     */
    fun timeMsUntilTargetTimeIntervalStart(timeMillis: Long): Long

    /**
     * @return  non-positive value as an indication that given point of time is not within the current schedule;
     *          a positive value of milliseconds remaining until the target active time interval end
     */
    fun timeMsUntilTargetTimeIntervalEnd(dateTime: LocalDateTime): Long

    /**
     * @return  non-positive value as an indication that given point of time is not within the current schedule;
     *          a positive value of milliseconds remaining until the target active time interval end
     */
    fun timeMsUntilTargetTimeIntervalEnd(timeMillis: Long): Long
}