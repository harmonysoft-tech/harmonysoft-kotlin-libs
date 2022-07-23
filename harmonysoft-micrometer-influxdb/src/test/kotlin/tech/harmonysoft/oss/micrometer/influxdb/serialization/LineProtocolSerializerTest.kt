package tech.harmonysoft.oss.micrometer.influxdb.serialization

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LineProtocolSerializerTest {

    private val serializer = LineProtocolSerializer()

    @Test
    fun `when long double value is used that it's truncated`() {
        testDoubleRepresentation(123456789.987654321, "123456789.987654")
    }

    @Test
    fun `when short double value is used then trailing zeroes are dropped`() {
        testDoubleRepresentation(1.2, "1.2")
    }

    @Test
    fun `when zero value is used then it's properly written`() {
        testDoubleRepresentation(0.0, "0")
    }

    private fun testDoubleRepresentation(value: Double, expected: String) {
        val buffer = StringBuilder()
        serializer.writeDouble(value, buffer)
        assertThat(buffer.toString()).isEqualToIgnoringCase(expected)
    }
}