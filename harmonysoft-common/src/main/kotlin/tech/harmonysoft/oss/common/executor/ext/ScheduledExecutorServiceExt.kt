package tech.harmonysoft.oss.common.schedule

import org.slf4j.LoggerFactory
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * We can't use standard [ScheduledExecutorService.scheduleWithFixedDelay] method when configuration
 * (delay and time unit) can be change at any time. That's why we might need an ability of rescheduling
 * task using actual runtime values after each execution. This extension solves that problem.
 *
 * Another feature it provides is that it catches any uncaught exceptions, logs them and doesn't kill
 * the current thread.
 */
fun ScheduledExecutorService.scheduleWithFixedDelay(
    startImmediately: Boolean,
    delayProvider: () -> Long,
    timeUnitProvider: () -> TimeUnit,
    command: () -> Unit
): ScheduledFuture<*> {
    val initialDelay = if (startImmediately) {
        0L
    } else {
        delayProvider()
    }

    val scheduledFutureRef = AtomicReference<ScheduledFuture<*>>()
    val runner = Runner(
        delayProvider = delayProvider,
        timeUnitProvider = timeUnitProvider,
        action = command,
        stopPredicate = {
            scheduledFutureRef.get()?.isCancelled ?: true
        },
        threadPool = this
    )
    return ScheduledFutureWrapper(
        delegate = schedule(runner, initialDelay, timeUnitProvider()),
        delayProvider = delayProvider,
        timeUnitProvider = timeUnitProvider
    ).apply {
        scheduledFutureRef.set(this)
    }
}

/**
 * As we have to reschedule task after each execution, we can't rely on [ScheduledFuture] returned
 * after first call to [ScheduledExecutorService.schedule] because it will be completed after the first
 * execution. We also can't cancel the whole rescheduling processing by cancelling this [ScheduledFuture].
 * But we still have to provide caller a tool to control task execution.
 *
 * This class wraps original [ScheduledFuture] and overrides methods that will return inconsistent
 * values from start or after the first execution. It provides custom cancellation implementation as well
 * as [isDone] method that don't rely on initial [ScheduledFuture] as all. We also need custom implementation
 * of [getDelay] method because initially task could be scheduled for immediate execution and in this case
 * wrapped [ScheduledFuture] will not provide the actual delay.
 */
private class ScheduledFutureWrapper<T>(
    private val delegate: ScheduledFuture<T>,
    private val delayProvider: () -> Long,
    private val timeUnitProvider: () -> TimeUnit
) : ScheduledFuture<T> by delegate {

    private val cancelled = AtomicBoolean()

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        cancelled.set(true)
        return true
    }

    override fun isCancelled(): Boolean {
        return cancelled.get()
    }

    override fun getDelay(unit: TimeUnit): Long {
        return unit.convert(delayProvider(), timeUnitProvider())
    }

    override fun isDone(): Boolean {
        return !cancelled.get()
    }
}

private class Runner<T>(
    private val delayProvider: () -> Long,
    private val timeUnitProvider: () -> TimeUnit,
    private val action: () -> T,
    private val stopPredicate: () -> Boolean,
    private val threadPool: ScheduledExecutorService
) : Runnable {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun run() {
        if (stopPredicate()) {
            return
        }

        try {
            action()
        } catch (e: Throwable) {
            logger.warn("Got unexpected exception during scheduled task execution", e)
        } finally {
            threadPool.schedule(this, delayProvider(), timeUnitProvider())
        }
    }
}