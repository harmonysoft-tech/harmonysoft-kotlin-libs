package tech.harmonysoft.oss.common.time.clock

import java.time.Clock
import java.time.ZoneId

class ZonedSystemClockProvider(
    zoneId: ZoneId
) : ClockProvider {

    private val clock = Clock.system(zoneId)

    override fun getData(): Clock {
        return clock
    }

    override fun withZone(zone: ZoneId): ClockProvider {
        return if (clock.zone == zone) {
            this
        } else {
            ClockProvider.forZone(zone)
        }
    }

    override fun refresh() {
    }

    override fun probe(): Clock {
        return data
    }

    override fun hashCode(): Int {
        return clock.zone.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return clock.zone == (other as? ZonedSystemClockProvider)?.clock?.zone
    }

    override fun toString(): String {
        return clock.zone.toString()
    }
}