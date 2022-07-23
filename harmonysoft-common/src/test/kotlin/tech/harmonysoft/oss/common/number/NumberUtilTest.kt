package tech.harmonysoft.oss.common.number

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class NumberUtilTest {

    @Test
    fun `when BigDecimals are normalized then they can be correctly compared via equals()`() {
        val bd1 = BigDecimal("0.00")
        val bd2 = BigDecimal.ZERO
        assertThat(bd1).isNotEqualTo(bd2)
        assertThat(NumberUtil.normalize(bd1)).isEqualTo(NumberUtil.normalize(bd2))
    }

    @Test
    fun `when BigDecimal is inexact then it's correctly normalized`() {
        val bd1 = BigDecimal("25.749984776168528")
        val bd2 = BigDecimal(25.749985)
        assertThat(bd1).isNotEqualTo(bd2)
        assertThat(NumberUtil.normalize(bd1)).isEqualTo(NumberUtil.normalize(bd2))
    }

    @Test
    fun `when double within precision boundaries is rounded then it's done correctly`() {
        assertThat(NumberUtil.round(1.0, 2)).isEqualTo(1.0)
        assertThat(NumberUtil.round(1.2, 1)).isEqualTo(1.2)
        assertThat(NumberUtil.round(1.23, 2)).isEqualTo(1.23)
        assertThat(NumberUtil.round(1.234, 4)).isEqualTo(1.234)
    }

    @Test
    fun `when double outside precision boundaries is rounded then it's done correctly`() {
        assertThat(NumberUtil.round(1.1, 0)).isEqualTo(1.0)
        assertThat(NumberUtil.round(1.5, 0)).isEqualTo(2.0)
        assertThat(NumberUtil.round(1.23, 1)).isEqualTo(1.2)
        assertThat(NumberUtil.round(1.29, 1)).isEqualTo(1.3)
    }

}