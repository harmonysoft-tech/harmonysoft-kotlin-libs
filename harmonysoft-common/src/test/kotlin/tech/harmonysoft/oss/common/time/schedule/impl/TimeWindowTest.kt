package tech.harmonysoft.oss.common.time.schedule.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalTime

internal class TimeWindowTest {

    @Test
    fun `when there are white spaces between start and end times then they are correctly parsed`() {
        val start = "10:01:03"
        val end = "11:02:03"
        val timeWindow = TimeWindow.parse("$start    -$end")
        assertThat(timeWindow.startTime).isEqualTo(LocalTime.parse(start))
        assertThat(timeWindow.endTime).isEqualTo(LocalTime.parse(end))
    }

    @Test
    fun `when placeholder is used then it's replaced by actual value`() {
        val start = "10:01:02"
        val end = TimeWindow.END_OF_DAY
        val timeWindow = TimeWindow.parse("$start-$end")
        assertThat(timeWindow.startTime).isEqualTo(LocalTime.parse(start))
        assertThat(timeWindow.endTime).isEqualTo(LocalTime.MAX)
    }

    @Test
    fun `when time windows are adjusted then they don't overlap`() {
        val window1 = TimeWindow.parse("10:00:00 - 11:00:00")
        val window2 = TimeWindow.parse("11:00:00 - 12:00:00")
        assertThat(window1.overlaps(window2)).isFalse()
        assertThat(window2.overlaps(window1)).isFalse()
    }

    @Test
    fun `when start time is the same as end time then it's rejected`() {
        assertThrows<IllegalArgumentException> {
            TimeWindow.parse("10:00:00 - 10:00:00")
        }
    }

    @Test
    fun `when start time is after end time then it's rejected`() {
        assertThrows<IllegalArgumentException> {
            TimeWindow.parse("10:01:00 - 10:00:00")
        }

    }
}