package tech.harmonysoft.oss.test.util

import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.ProcessingResult

object TimedUtil {

    val DEFAULT_FREQUENCY_MS = System.getProperty("timed.action.frequency.ms.default")?.takeIf {
        it.isNotBlank()
    }?.toLong() ?: 500L

    val DEFAULT_TTL_SECONDS = System.getProperty("timed.action.ttl.seconds.default")?.takeIf {
        it.isNotBlank()
    }?.toLong() ?: 10L

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Waits for the specified amount of time until target condition identified by the given checker function
     * happens.
     *
     * @return  successful result if given checker reports a success within the wait period;
     *          failed result otherwise
     */
    fun <T> waitForCondition(
        description: String = "<no description>",
        ttlSeconds: Long = DEFAULT_TTL_SECONDS,
        frequencyMs: Long = DEFAULT_FREQUENCY_MS,
        checker: () -> ProcessingResult<T, String>
    ): ProcessingResult<T, String> {
        val endTimeMs = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ttlSeconds)
        logger.info("starting timed verification for the target condition '{}' to happen", description)
        val firstCheckResult = checker()
        if (firstCheckResult.success) {
            return firstCheckResult
        }
        var error = firstCheckResult
        while (System.currentTimeMillis() < endTimeMs) {
            val result = checker()
            if (result.success) {
                logger.info("verified that '{}' happened", description)
                return result
            } else {
                error = result
                Thread.sleep(frequencyMs)
            }
        }
        logger.info(
            "condition '{}' didn't happen in {} seconds, last error: {}",
            description, ttlSeconds, error.failureValue
        )
        return error
    }
}