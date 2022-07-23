package tech.harmonysoft.oss.test.util

import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.common.data.DataProviderStrategy
import tech.harmonysoft.oss.common.util.ObjectUtil
import tech.harmonysoft.oss.test.util.TestUtil.fail
import java.util.concurrent.TimeUnit

object VerificationUtil {

    val POLLED_VERIFICATION_CHECK_FREQUENCY_MS = System.getProperty("verification.polled.frequency.ms")?.takeIf {
        it.isNotBlank()
    }?.toLong() ?: 500L

    val POSITIVE_POLLED_VERIFICATION_TTL_SECONDS = System.getProperty("verification.polled.positive.ttl.seconds")?.takeIf {
        it.isNotBlank()
    }?.toLong() ?: 10L

    val NEGATIVE_POLLED_VERIFICATION_TTL_SECONDS = System.getProperty("verification.polled.negative.ttl.seconds")?.takeIf {
        it.isNotBlank()
    }?.toLong() ?: 2L

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Timeout-based utility for verifying that particular condition eventually happens. It's convenient in case
     * of verifying concurrent processing results when we expect to observe particular state within particular
     * timeout
     */
    fun verifyConditionHappens(
        description: String = "<no-description>",
        checkTtlSeconds: Long = POSITIVE_POLLED_VERIFICATION_TTL_SECONDS,
        checkFrequencyMs: Long = POLLED_VERIFICATION_CHECK_FREQUENCY_MS,
        checker: () -> ProcessingResult<Unit, String>
    ) {
        val endTimeMs = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(checkTtlSeconds)
        var error = "<not provided>"
        logger.info("Starting timed verification for the target condition '{}' to happen", description)
        while (System.currentTimeMillis() < endTimeMs) {
            val result = checker()
            if (result.success) {
                logger.info("Verified that '{}' happened", description)
                return
            } else {
                error = result.failureValue
                Thread.sleep(checkFrequencyMs)
            }
        }

        fail("target condition '$description' is not observed within $checkTtlSeconds seconds. Last "
                + "verification failure error: '$error'")
    }

    /**
     * A complement to [verifyConditionHappens], is used in situations when we want to ensure that particular
     * condition doesn't happen within particular timeout
     */
    fun verifyConditionDoesNotHappen(
        description: String = "<no-description>",
        checkTtlSeconds: Long = NEGATIVE_POLLED_VERIFICATION_TTL_SECONDS,
        checkFrequencyMs: Long = POLLED_VERIFICATION_CHECK_FREQUENCY_MS,
        checker: () -> ProcessingResult<Unit, String>
    ) {
        val endTimeMs = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(checkTtlSeconds)
        logger.info("Starting timed verification for the unexpected condition '{}' not to happen", description)
        while (System.currentTimeMillis() < endTimeMs) {
            val result = checker()
            if (result.success) {
                if (System.currentTimeMillis() >= endTimeMs) {
                    break
                }
                Thread.sleep(checkFrequencyMs)
            } else {
                fail("unexpected condition is detected: '${result.failureValue}'")
            }
        }
        logger.info("Verified that condition '{}' didn't happen", description)
    }

    fun <D, K> verifyTheSame(
        expected: Set<D>,
        actual: Set<D>,
        keys: Set<K>,
        retrievalStrategy: DataProviderStrategy<D, K>
    ) {
        val result = compare(
            expected = expected,
            actual = actual,
            keys = keys,
            retrievalStrategy = retrievalStrategy
        )
        if (!result.success) {
            fail(result.failureValue)
        }
    }

    fun <D, K> compare(
        expected: Set<D>,
        actual: Set<D>,
        keys: Set<K>,
        retrievalStrategy: DataProviderStrategy<D, K>,
        equalityChecker: (K, Any?, Any?) -> Boolean = { _, left, right -> ObjectUtil.areEqual(left, right)}
    ): ProcessingResult<Unit, String> {
        val unmatchedExpected = mutableSetOf<D>()
        val unmatchedActual = actual.toMutableSet()
        for (e in expected) {
            val matched = unmatchedActual.removeIf {
                compare(e, it, keys, retrievalStrategy, equalityChecker).success
            }
            if (!matched) {
                unmatchedExpected += e
            }
        }

        return when {
            unmatchedExpected.isEmpty() && unmatchedActual.isEmpty() -> ProcessingResult.success()
            unmatchedExpected.size == 1 && unmatchedActual.size == 1 -> compare(
                unmatchedExpected.first(),
                unmatchedActual.first(),
                keys,
                retrievalStrategy,
                equalityChecker
            )
            else -> {
                val unmatchedError = unmatchedExpected.takeIf { it.isNotEmpty() }?.let {
"""
${it.size} expected record(s) are not found:
  *) ${it.joinToString("\n*)  ")}
""".trimIndent()
                }
                val actualError = unmatchedActual.takeIf { it.isNotEmpty() }?.let {
"""
${it.size} unexpected record(s) are found:
  *) ${it.joinToString("\n*)  ")}
""".trimIndent()
                }
                when {
                    actualError == null && unmatchedError != null -> ProcessingResult.failure(unmatchedError)
                    actualError != null && unmatchedError == null -> ProcessingResult.failure(actualError)
                    else -> ProcessingResult.failure("$unmatchedError\n$actualError")
                }
            }
        }
    }

    fun <D, K> verifyTheSame(
        expected: List<D>,
        actual: List<D>,
        keys: Set<K>,
        retrievalStrategy: DataProviderStrategy<D, K>
    ) {
        val result = compare(
            expected = expected,
            actual = actual,
            keys = keys,
            retrievalStrategy = retrievalStrategy
        )
        if (!result.success) {
            fail(result.failureValue)
        }
    }

    fun <D, K> compare(
        expected: List<D>,
        actual: List<D>,
        keys: Set<K>,
        retrievalStrategy: DataProviderStrategy<D, K>,
        equalityChecker: (K, Any?, Any?) -> Boolean = { _, left, right -> ObjectUtil.areEqual(left, right)}
    ): ProcessingResult<Unit, String> {
        val errors = mutableListOf<String>()
        if (expected.size > actual.size) {
            expected.subList(actual.size, expected.size).forEach {
                errors += "failed to find data record in the actual results: $it"
            }
        }

        if (actual.size > expected.size) {
            actual.subList(expected.size, actual.size).forEach {
                errors += "unexpected data record in the actual results: $it"
            }
        }

        for ((e, a) in expected.zip(actual)) {
            val checkResult = compare(e, a, keys, retrievalStrategy, equalityChecker)
            if (!checkResult.success) {
                errors += checkResult.failureValue
            }
        }

        return if (errors.isEmpty()) {
            ProcessingResult.success()
        } else {
            ProcessingResult.failure("""
found ${errors.size} error(s):
  *) ${errors.joinToString("\n  *)")}
            """.trimIndent())
        }
    }

    fun <D, K> compare(
        expected: D,
        actual: D,
        keys: Set<K>,
        retrievalStrategy: DataProviderStrategy<D, K>,
        equalityChecker: (K, Any?, Any?) -> Boolean = { _, left, right -> ObjectUtil.areEqual(left, right)}
    ): ProcessingResult<Unit, String> {
        val mismatches = mutableListOf<String>()
        for (key in keys) {
            val expectedValue = retrievalStrategy.getData(expected, key)
            val actualValue = retrievalStrategy.getData(actual, key)
            if (!equalityChecker(key, expectedValue, actualValue)) {
                mismatches += "expected $key = $expectedValue but got $actualValue"
            }
        }
        return if (mismatches.isEmpty()) {
            ProcessingResult.success()
        } else {
            ProcessingResult.failure("""
found ${mismatches.size} mismatch(es) in a data record:
  *) ${mismatches.joinToString("\n    *) ")}
expected data: $expected
actual data: $actual
            """.trimIndent())
        }
    }
}