package tech.harmonysoft.oss.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProcessingResultTest {

    @Test
    fun `when success value is used then attempt to access failure throws an exception`() {
        assertThrows<IllegalStateException> {
            ProcessingResult.success<String>().failureValue
        }
    }

    @Test
    fun `when success value is used then it's exposed`() {
        val value = "asd"
        val result = ProcessingResult.success<String, Unit>(value)
        assertThat(result.success).isTrue()
        assertThat(result.successValue).isEqualTo(value)
    }

    @Test
    fun `when failure value is used then attempt to access success throws an exception`() {
        assertThrows<IllegalStateException> {
            ProcessingResult.failure<Unit, Unit>(Unit).successValue
        }
    }

    @Test
    fun `when failure value is used then it's exposed`() {
        val value = "asd"
        val result = ProcessingResult.failure<Unit, String>(value)
        assertThat(result.success).isFalse()
        assertThat(result.failureValue).isEqualTo(value)
    }

    @Test
    fun `when success() then it works as expected`() {
        val result = ProcessingResult.success<String>()
        assertThat(result.success).isTrue()
        assertThat(result.successValue).isEqualTo(Unit)
    }

    @Test
    fun `when mapError() is used then it works as expected`() {
        val error = "my-error"
        val from = ProcessingResult.failure<Unit, String>(error)
        val to: ProcessingResult<Collection<Int>, String> = from.mapError()
        assertThat(to.success).isFalse()
        assertThat(to.failureValue).isEqualTo(error)
    }

    @Test
    fun `when nullable type is used then it works as expected`() {
        val result: ProcessingResult<String?, String> = ProcessingResult.success(null)
        assertThat(result.success).isTrue()
        assertThat(result.successValue).isNull()
    }
}