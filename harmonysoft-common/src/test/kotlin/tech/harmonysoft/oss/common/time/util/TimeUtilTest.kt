package tech.harmonysoft.oss.common.time.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.harmonysoft.oss.common.time.util.TimeUtil.Millis

internal class TimeUtilTest {

    @Test
    fun `when duration is less than minute then it's described correctly`() {
        val duration = Millis.SECOND * 14 + 1
        assertThat(TimeUtil.describeDuration(duration)).isEqualTo("14 seconds")
    }

    @Test
    fun `when duration is less than hour then it's described correctly`() {
        val duration = Millis.MINUTE + Millis.SECOND
        assertThat(TimeUtil.describeDuration(duration)).isEqualTo("1 minute 1 second")
    }

    @Test
    fun `when duration doesn't have a unit then it's described correctly`() {
        // we don't have seconds here
        val duration = Millis.MINUTE * 2
        assertThat(TimeUtil.describeDuration(duration)).isEqualTo("2 minutes")
    }

    @Test
    fun `when duration is more than hour then it's described correctly`() {
        val duration = Millis.HOUR * 2 + Millis.SECOND * 59
        assertThat(TimeUtil.describeDuration(duration)).isEqualTo("2 hours 59 seconds")
    }

    @Test
    fun `when duration is more than a day then it's described correctly`() {
        val duration =
            Millis.DAY * 10 +
            Millis.HOUR * 9 +
            Millis.MINUTE * 18 +
            Millis.SECOND * 59
        assertThat(TimeUtil.describeDuration(duration)).isEqualTo("1 week 3 days 9 hours 18 minutes 59 seconds")
    }

}