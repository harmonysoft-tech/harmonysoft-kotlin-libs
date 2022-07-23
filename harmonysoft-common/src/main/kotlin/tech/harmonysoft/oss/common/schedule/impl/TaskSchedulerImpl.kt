package tech.harmonysoft.oss.common.schedule.impl

import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.schedule.ScheduledTask
import tech.harmonysoft.oss.common.schedule.TaskScheduler
import tech.harmonysoft.oss.common.time.clock.ClockProvider
import java.time.LocalDateTime
import java.time.temporal.ChronoField
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TaskSchedulerImpl(
    private val schedulerId: String,
    private val clockProvider: ClockProvider,
    private val threadPool: ScheduledExecutorService
) : TaskScheduler {

    private val pending = mutableMapOf<String/* task id */, Future<*>>()
    private val pendingTasksLock = ReentrantLock()
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun schedule(tasks: Collection<ScheduledTask>, callback: TaskScheduler.Callback) {
        logger.info("Got a request to apply new tasks schedule for {}: {}", schedulerId, tasks)
        cleanStaleTasks(tasks.map { it.id }.toSet())
        for (task in tasks) {
            schedule(
                task = task,
                callback = callback,
                reschedule = false
            )
        }
    }

    @Synchronized
    private fun schedule(
        task: ScheduledTask,
        callback: TaskScheduler.Callback,
        reschedule: Boolean,
        anchorTimeMillis: Long? = null
    ) {
        val clock = clockProvider.data
        val now = clock.millis()

        // we add '1' here because it's possible that this method is called when target task is triggered and
        // we're in the same millisecond. The goal is to ensure that we passed active trigger time
        val nextTriggerTime = task.schedule.getNextValidTimeAfter(Date((anchorTimeMillis ?: now) + 1))
        scheduleTaskWithDelay(
            task = task,
            delayMs = nextTriggerTime.time - now,
            nextTriggerTime = nextTriggerTime.time,
            reschedule = reschedule,
            callback = callback
        )
    }

    @Synchronized
    private fun scheduleTaskWithDelay(
        task: ScheduledTask,
        delayMs: Long,
        nextTriggerTime: Long,
        reschedule: Boolean,
        callback: TaskScheduler.Callback
    ) {
        pendingTasksLock.withLock {
            val previous = pending.remove(task.id)
            if (previous == null && reschedule) {
                return
            }

            previous?.cancel(false)

            logger.info(
                "Scheduling task '{}' to run by scheduler {} in {} ms (at {})",
                task.id, schedulerId, delayMs,
                LocalDateTime.now(clockProvider.data).plus(delayMs, ChronoField.MILLI_OF_DAY.baseUnit)
            )
            val future = threadPool.schedule(
                { runTask(task, nextTriggerTime, callback) },
                delayMs,
                TimeUnit.MILLISECONDS
            )
            pending[task.id] = future
        }
    }

    private fun runTask(
        task: ScheduledTask,
        anchorTimeMillis: Long,
        callback: TaskScheduler.Callback
    ) {
        val now = clockProvider.data.millis()
        val delay = anchorTimeMillis - now
        if (delay > 0) {
            logger.info(
                "Task '{}' in scheduler {} is triggered before the target trigger time, now: {}, anchor time: {}, "
                + "will re-schedule", task.id, schedulerId, now, anchorTimeMillis
            )
            scheduleTaskWithDelay(
                task = task,
                delayMs = delay,
                nextTriggerTime = anchorTimeMillis,
                reschedule = true,
                callback = callback
            )
            return
        }

        logger.info("Task '{}' is triggered by scheduler {}", task.id, schedulerId)
        try {
            callback.onTriggered(task.id)
        } catch (e: Throwable) {
            logger.warn("Got an unexpected exception on attempt to process task '{}' by scheduler {}",
                        task.id, schedulerId, e)
        } finally {
            schedule(
                task = task,
                callback = callback,
                reschedule = true,
                anchorTimeMillis = anchorTimeMillis
            )
        }
    }

    fun clear() {
        val tasks = pendingTasksLock.withLock {
            pending.values.toSet().apply {
                pending.clear()
            }
        }
        for (task in tasks) {
            task.cancel(false)
        }
    }

    private fun cleanStaleTasks(activeTaskIds: Set<String>) {
        pendingTasksLock.withLock {
            val taskIds = pending.keys.toSet()
            for (taskId in taskIds) {
                if (!activeTaskIds.contains(taskId)) {
                    logger.info(
                        "Cancelling stale task '{}' in scheduler {} as the task is not in the new schedule ({})",
                        taskId, schedulerId, activeTaskIds
                    )
                    pending.remove(taskId)?.apply {
                        cancel(false)
                    }
                }
            }
        }
    }
}