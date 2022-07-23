package tech.harmonysoft.oss.common.thread

import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo

object ThreadUtil {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun logThreadDump(message: String, filter: (ThreadInfo) -> Boolean) {
        val threadMxBean = ManagementFactory.getThreadMXBean()
        val threadDump = threadMxBean.getThreadInfo(threadMxBean.allThreadIds, Int.MAX_VALUE)
            .filter {
                it != null && filter(it)
            }
            .joinToString(separator = "\n\n") { threadInfo ->
                """|${threadInfo.threadName}
                       |    ${Thread.State::class.simpleName}: ${threadInfo.threadState}
                       |${threadInfo.stackTrace.joinToString(separator = "\n") {"        at $it" }}
                """.trimMargin()
            }
        logger.info("{}\n{}", message, threadDump)
    }
}