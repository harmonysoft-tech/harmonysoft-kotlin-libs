package tech.harmonysoft.oss.common.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.harmonysoft.oss.test.util.TestUtil.fail

internal class ExceptionUtilTest {

    @Test
    fun `when exception is printed then it's done correctly`() {
        try {
            outerMethodToThrowException()
            fail("this place should not be reached")
        } catch (e: Exception) {
            val expectedOuterExceptionText = normalizeLineEndings("""
                java.lang.IllegalArgumentException: outer-exception
                	at tech.harmonysoft.oss.common.exception.ExceptionUtilTest.outerMethodToThrowException(ExceptionUtilTest.kt:35)
                	at tech.harmonysoft.oss.common.exception.ExceptionUtilTest.when exception is printed then it's done correctly(ExceptionUtilTest.kt:12)
            """.trimIndent())
            val expectedInnerExceptionText = normalizeLineEndings("""
                Caused by: java.lang.RuntimeException: inner-exception
                	at tech.harmonysoft.oss.common.exception.ExceptionUtilTest.innerMethodToThrowException(ExceptionUtilTest.kt:40)
                	at tech.harmonysoft.oss.common.exception.ExceptionUtilTest.outerMethodToThrowException(ExceptionUtilTest.kt:33)
            """.trimIndent())
            val actual = normalizeLineEndings(ExceptionUtil.exceptionToString(e))
            assertThat(actual).contains(expectedOuterExceptionText)
            assertThat(actual).contains(expectedInnerExceptionText)
        }
    }

    private fun outerMethodToThrowException() {
        try {
            innerMethodToThrowException()
        } catch (e: Exception) {
            throw IllegalArgumentException("outer-exception", e)
        }
    }

    private fun innerMethodToThrowException() {
        throw RuntimeException("inner-exception")
    }

    private fun normalizeLineEndings(raw: String): String {
        return raw.replace("\r\n", "\r").replace("\n", "\r")
    }
}