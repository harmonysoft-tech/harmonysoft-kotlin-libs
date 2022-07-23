package tech.harmonysoft.oss.common.schedule

import org.quartz.CronExpression

data class ScheduledTask(
    val id: String,

    /**
     * [CronExpression] uses default time zone by default. We experienced problems with that when target
     * application was running on a machine which default time zone differed from the target application
     * time zone.
     *
     * That's why it's mandatory to [specify time zone][CronExpression.setTimeZone] in this argument.
     */
    val schedule: CronExpression
) {

    init {
        // we can't use CronExpression.getTimeZone() because it applied default time zone if no time
        // zone was explicitly set. That's why we get the actual value via reflection here
        val field = CronExpression::class.java.getDeclaredField("timeZone")
        field.isAccessible = true
        field.get(schedule) ?: throw IllegalArgumentException(
            "detected an attempt ot create a scheduled task '$id' with a schedule where "
            + "time zone is undefined: $schedule"
        )
    }
}