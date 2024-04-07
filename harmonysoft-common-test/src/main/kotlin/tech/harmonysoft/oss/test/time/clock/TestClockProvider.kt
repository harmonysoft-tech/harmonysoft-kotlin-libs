package tech.harmonysoft.oss.test.time.clock

import jakarta.annotation.Priority
import tech.harmonysoft.oss.common.di.DiConstants
import tech.harmonysoft.oss.common.time.clock.ClockProvider
import tech.harmonysoft.oss.test.TestAware
import java.time.Clock
import java.time.ZoneId
import jakarta.inject.Named

@Priority(DiConstants.LIB_PRIMARY_PRIORITY)
@Named
class TestClockProvider : ClockProvider, TestAware {

    private val clock = TestClock()

    override fun getData(): TestClock {
        return clock
    }

    override fun probe(): Clock {
        return data
    }

    override fun onTestStart() {
        clock.onTestEnd()
    }

    override fun onTestEnd() {
        clock.onTestEnd()
    }

    override fun withZone(zone: ZoneId): ClockProvider {
        return apply {
            clock.withZone(zone)
        }
    }

    override fun refresh() {
    }
}