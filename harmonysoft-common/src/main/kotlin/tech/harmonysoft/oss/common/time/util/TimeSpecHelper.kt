package tech.harmonysoft.oss.common.time.util

import tech.harmonysoft.oss.common.time.clock.ClockProvider
import java.time.ZonedDateTime
import javax.inject.Named

/**
 * There are use-cases when we want to specify particular offsets for target actions in configs, e.g.
 * `T + 1` stands for tomorrow, `T` for today, etc. This method allows to get target time for the offset
 * defined by the given spec.
 *
 * Supported formats:
 * * `T` - current time from [ClockProvider]
 * * `T - N` - current time minus `N` days, e.g. `T - 1`, `T - 2`, etc
 * * `T + N` - current time minus `N` days, e.g. `T + 1`, `T + 2`, etc
 */
@Named
class TimeSpecHelper(
    private val clockProvider: ClockProvider
) {

    fun getTime(timeSpec: String): ZonedDateTime {
        return maybeGetTime(timeSpec) ?: throw IllegalArgumentException(
            "can't parse time spec '$timeSpec'"
        )
    }

    fun maybeGetTime(timeSpec: String): ZonedDateTime? {
        val now = ZonedDateTime.now(clockProvider.data)
        if (timeSpec == "T") {
            return now
        }

        return TimePattern.T_MINUS.matchEntire(timeSpec)?.let { now.minusDays(it.groupValues[1].toLong()) }
               ?: TimePattern.T_PLUS.matchEntire(timeSpec)?.let { now.plusDays(it.groupValues[1].toLong()) }
    }

    object TimePattern {
        val T_MINUS = """T\s*-\s*(\d+)""".toRegex()
        val T_PLUS = """T\s*\+\s*(\d+)""".toRegex()
    }
}