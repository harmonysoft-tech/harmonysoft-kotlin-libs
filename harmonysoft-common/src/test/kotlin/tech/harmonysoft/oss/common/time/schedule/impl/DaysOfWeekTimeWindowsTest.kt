package tech.harmonysoft.oss.common.time.schedule.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek

internal class DaysOfWeekTimeWindowsTest {

    private lateinit var windows: DaysOfWeekTimeWindows

    @BeforeEach
    fun setUp() {
        windows = DaysOfWeekTimeWindows(
            daysOfWeek = DayOfWeek.values().toSet(),
            _timeWindows = setOf(
                TimeWindow.parse("10:00 - 11:00"),
                TimeWindow.parse("15:00 - 16:00")
            )
        )
    }

    @Test
    fun `when overlapping time windows are provided then they are rejected`() {
        assertThrows<IllegalArgumentException> {
            DaysOfWeekTimeWindows(
                daysOfWeek = DayOfWeek.values().toSet(),
                _timeWindows = setOf(
                    TimeWindow.parse("10:00 - 10:02"),
                    TimeWindow.parse("10:01 - 10:03")
                )
            )
        }
    }
}