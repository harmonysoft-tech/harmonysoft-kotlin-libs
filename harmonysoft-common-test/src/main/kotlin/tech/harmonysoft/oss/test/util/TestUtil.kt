package tech.harmonysoft.oss.test.util

import org.slf4j.LoggerFactory

object TestUtil {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Sometimes tests fail due to race conditions, in that situation it's important to be able tracing
     * back the problem from test execution logs and it's crucial to see timings of events happened
     * during that. Exceptions don't include time info, that's we have this utility which not only
     * throw an assertion error but also logs it (assuming that the logger is configured to output time info)
     */
    fun fail(error: String): Nothing {
        logger.error(error)
        throw AssertionError(error)
    }
}