package tech.harmonysoft.oss.common.time.ext

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class LocalDateExtKtTest {

    private val date1 = LocalDate.parse("2021-01-29")
    private val date2 = LocalDate.parse("2021-01-30")

    @Test
    fun `when range operator is used with LocalDate then it works correctly`() {
        assertThat((date1..date2).toSet()).containsOnly(date1, date2)
    }

    @Test
    fun `when range operator is used with LocalDate then it can be iterated`() {
        val actual = mutableListOf<LocalDate>()
        for (date in date1..date2) {
            actual += date
        }
        assertThat(actual).containsExactly(date1, date2)
    }

    @Test
    fun `when range with end date before start date is used then it's properly rejected`() {
        assertThrows<IllegalArgumentException> {
            date2..date1
        }
    }

    @Test
    fun `when range with end date equal to start date is used then it works correctly`() {
        assertThat((date1..date1).toSet()).containsOnly(date1)
    }
}