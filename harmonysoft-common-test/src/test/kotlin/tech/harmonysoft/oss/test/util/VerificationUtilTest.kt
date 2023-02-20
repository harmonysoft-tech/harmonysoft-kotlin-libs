package tech.harmonysoft.oss.test.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.harmonysoft.oss.common.data.DataProviderStrategy

internal class VerificationUtilTest {

    @Test
    fun `when lists comparison fails then error message is properly indented`() {
        val expected = listOf(
            mapOf("key1" to "value11", "key2" to "value21"),
            mapOf("key1" to "value21", "key2" to "value22")
        )
        val actual = listOf<Map<String, String>>(
            emptyMap(),
            emptyMap(),
            emptyMap()
        )
        val result = VerificationUtil.compare(expected, actual, expected.first().keys, DataProviderStrategy.fromMap())
        assertThat(result.failureValue).isEqualTo("""
found 3 error(s):
  *) unexpected data record in the actual results: {}
  *) found 2 mismatch(es) in a data record:
    1) expected key1 = value11 but got null
    2) expected key2 = value21 but got null
    * expected data: {key1=value11, key2=value21}
    * actual data: {}
  *) found 2 mismatch(es) in a data record:
    1) expected key1 = value21 but got null
    2) expected key2 = value22 but got null
    * expected data: {key1=value21, key2=value22}
    * actual data: {}
      """.trim())
    }
}