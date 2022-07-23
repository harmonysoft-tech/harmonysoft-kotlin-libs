package tech.harmonysoft.oss.common.executor.ext

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tech.harmonysoft.oss.common.schedule.scheduleWithFixedDelay
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class ScheduledExecutorServiceExtKtTest {

    private lateinit var threadPool: ScheduledExecutorService
    private lateinit var executionsCount: AtomicInteger
    private lateinit var latch: CountDownLatch

    @BeforeEach
    fun setUp() {
        threadPool = Executors.newScheduledThreadPool(1)
        executionsCount = AtomicInteger()
        latch = CountDownLatch(EXECUTIONS_COUNT)
    }

    private fun execute() {
        executionsCount.incrementAndGet()
        latch.countDown()
    }

    @Test
    fun `when cancellable task is configured with default delay then it executes until cancelled`() {
        val cancellable = threadPool.scheduleWithFixedDelay(
            startImmediately = false,
            delayProvider = { DEFAULT_DELAY_MS },
            timeUnitProvider = { TimeUnit.MILLISECONDS },
            command = this::execute
        )

        assertThat(latch.await(WAIT_TIMOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
        assertThat(executionsCount.get()).isEqualTo(EXECUTIONS_COUNT)

        cancellable.cancel(false)
        Thread.sleep(DEFAULT_DELAY_MS * 3)
        assertThat(executionsCount.get()).isEqualTo(EXECUTIONS_COUNT)
    }

    companion object {

        const val DEFAULT_DELAY_MS = 200L
        const val WAIT_TIMOUT_SECONDS = 10L
        const val EXECUTIONS_COUNT = 3
    }
}