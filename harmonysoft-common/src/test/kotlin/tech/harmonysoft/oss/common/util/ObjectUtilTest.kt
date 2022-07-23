package tech.harmonysoft.oss.common.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class ObjectUtilTest {

    @Test
    fun `when nulls are given to areEqual() then they are processed correctly`() {
        assertThat(ObjectUtil.areEqual(null, null)).isTrue()
    }

    @Test
    fun `when one areEqual() argument is null and another is not then they are processed correctly`() {
        assertThat(ObjectUtil.areEqual(1, null)).isFalse()
        assertThat(ObjectUtil.areEqual(null, "a")).isFalse()
    }

    @Test
    fun `when areEqual() arguments are the same objects then they are processed correctly`() {
        Any().apply {
            assertThat(ObjectUtil.areEqual(this, this)).isTrue()
        }
    }

    @Test
    fun `when areEqual() arguments are the same by compareTo() but different by equals() then they are processed correctly`() {
        assertThat(ObjectUtil.areEqual(BigDecimal("101.00"), BigDecimal("101.0000"))).isTrue()
    }
}