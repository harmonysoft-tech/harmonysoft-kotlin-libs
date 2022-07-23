package tech.harmonysoft.oss.common.schedule

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.quartz.CronExpression

internal class ScheduledTaskTest {

    @Test
    fun `when no time zone is defined then that is reported`() {
        assertThrows<IllegalArgumentException> {
            ScheduledTask("some-id", CronExpression("1 0 3 ? * *"))
        }
    }
}