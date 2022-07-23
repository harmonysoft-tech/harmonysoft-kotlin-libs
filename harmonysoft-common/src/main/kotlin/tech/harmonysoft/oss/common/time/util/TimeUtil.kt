package tech.harmonysoft.oss.common.time.util

import java.time.ZoneId
import java.util.concurrent.TimeUnit

object TimeUtil {

    object Millis {
        val WEEK = TimeUnit.DAYS.toMillis(7)
        val DAY = TimeUnit.DAYS.toMillis(1)
        val HOUR = TimeUnit.HOURS.toMillis(1)
        val MINUTE = TimeUnit.MINUTES.toMillis(1)
        val SECOND = TimeUnit.SECONDS.toMillis(1)
    }

    object Zone {
        val UTC = ZoneId.of("UTC")
    }

    fun describeDuration(millis: Long): String {
        var remaining = millis
        return buildString {
            remaining = maybeAppend(remaining, this, Millis.WEEK, "week")
            remaining = maybeAppend(remaining, this, Millis.DAY, "day")
            remaining = maybeAppend(remaining, this, Millis.HOUR, "hour")
            remaining = maybeAppend(remaining, this, Millis.MINUTE, "minute")
            remaining = maybeAppend(remaining, this, Millis.SECOND, "second")
        }
    }

    private fun maybeAppend(remaining: Long, holder: StringBuilder, unitValue: Long, unitName: String): Long {
        val units = remaining / unitValue
        if (units <= 0) {
            return remaining
        }
        if (holder.isNotEmpty()) {
            holder.append(" ")
        }
        holder.append(units).append(" ").append(unitName)
        if (units > 1) {
            holder.append("s")
        }
        return remaining % unitValue
    }
}