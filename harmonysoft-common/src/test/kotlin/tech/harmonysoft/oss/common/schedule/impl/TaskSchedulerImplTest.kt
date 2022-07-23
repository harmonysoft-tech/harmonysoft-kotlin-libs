package tech.harmonysoft.oss.common.schedule.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.quartz.CronExpression
import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.schedule.ScheduledTask
import tech.harmonysoft.oss.common.schedule.TaskScheduler
import tech.harmonysoft.oss.test.time.clock.TestClockProvider
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

internal class TaskSchedulerImplTest {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val clockProvider = TestClockProvider()

    private lateinit var scheduler: TaskScheduler

    @BeforeEach
    fun setUp() {
        scheduler = TaskSchedulerImpl("test", clockProvider, Executors.newScheduledThreadPool(2))
    }

    @AfterEach
    fun tearDown() {
        clockProvider.onTestEnd()
    }

    @Test
    fun `when a schedule is provided then it's respected`() {
        setTime("03:59:59.900")
        val handle = schedule(TASK_ID, "*/1 0 4 ? * *")

        setTime("04:00:00.000")
        verifyTriggered(handle, TASK_ID)

        handle.reset()
        verifyNotTriggered(handle, 400)

        setTime("04:00:01.000")
        verifyTriggered(handle, TASK_ID, 1000)
    }

    @Test
    fun `when schedule is updated then it's respected`() {
        setTime("03:00:00.000")
        val handle1 = schedule(TASK_ID, "0 0 4 ? * *")
        verifyNotTriggered(handle1)

        setTime("04:00:59.900")
        val handle2 = schedule(TASK_ID, "0 1 4 ? * *")
        verifyNotTriggered(handle1)
        verifyNotTriggered(handle2)

        setTime("04:01:00.000")
        verifyNotTriggered(handle1)
        verifyTriggered(handle2, TASK_ID, 1000)
    }

    @Test
    fun `when a task becomes stale then it's automatically removed from schedule`() {
        setTime("03:00:00.900")
        val handle1 = schedule(TASK_ID, "1 0 3 ? * *")
        verifyNotTriggered(handle1)

        val newTaskId = TASK_ID + 2
        val handle2 = schedule(newTaskId, "1 0 3 ? * *")

        setTime("03:00:01.000")
        verifyNotTriggered(handle1)
        verifyTriggered(handle2, newTaskId, 1000)
    }

    @Test
    fun `when new schedule is being applied during task execution then no race condition occurs`() {
        val handle2 = AtomicReference<TaskHandle>()
        val newTaskId = TASK_ID + 2
        val task2HandleSemaphore = Semaphore(0)

        setTime("03:00:00.900")
        val handle1 = schedule(TASK_ID, "*/1 0 3 ? * *") {
            handle2.set(schedule(newTaskId, "2 0 3 ? * *"))
            task2HandleSemaphore.release()
        }
        setTime("03:00:01.900")
        verifyTriggered(handle1, TASK_ID)
        handle1.reset()
        task2HandleSemaphore.acquire()

        setTime("03:00:02.000")
        verifyNotTriggered(handle1)
        verifyTriggered(handle2.get(), newTaskId, 1000)
    }

    private fun setTime(time: String, daysToAdd: Long = 0L) {
        clockProvider.data.withInstant(
            LocalTime
                .parse(time)
                .atDate(LocalDate.now().plusDays(daysToAdd))
                .atZone(clockProvider.data.zone)
                .toInstant()
                .toEpochMilli()
        )
    }

    private fun schedule(taskId: String, schedule: String, additionalTriggerCallback: (() -> Unit)? = null): TaskHandle {
        return TaskHandle(Semaphore(0), AtomicReference()).apply {
            val tasks = listOf(ScheduledTask(taskId, CronExpression(schedule).apply {
                timeZone = TimeZone.getTimeZone(clockProvider.data.zone)
            }))
            scheduler.schedule(tasks) { taskId ->
                triggeredTaskIdRef.set(taskId)
                semaphore.release()
                additionalTriggerCallback?.invoke()
            }
        }
    }

    private fun verifyNotTriggered(handle: TaskHandle, ttlMillis: Long = 200L) {
        logger.info("Start waiting for up to {} ms to confirm that target task is not triggered", ttlMillis)
        assertThat(handle.semaphore.tryAcquire(ttlMillis, TimeUnit.MILLISECONDS)).isFalse()
        logger.info("Finished waiting up to {} ms on ensuring that target task is not triggered", ttlMillis)
        assertThat(handle.triggeredTaskIdRef.get()).isNull()
    }

    private fun verifyTriggered(handle: TaskHandle, expectedTriggeredTaskId: String, ttlMillis: Long = 200L) {
        logger.info("Start waiting for up to {} ms to confirm that task '{}' is triggered",
                    ttlMillis, expectedTriggeredTaskId)
        assertThat(handle.semaphore.tryAcquire(ttlMillis, TimeUnit.MILLISECONDS))
            .describedAs("expected that task '$expectedTriggeredTaskId' would be triggered but it was not")
            .isTrue()
        logger.info("Finished waiting up to {} ms on ensuring that target task '{}' is triggered",
                    ttlMillis, expectedTriggeredTaskId)
        assertThat(handle.triggeredTaskIdRef.get()).isEqualTo(expectedTriggeredTaskId)
    }

    companion object {
        const val TASK_ID = "test-task"
    }

    data class TaskHandle(
        val semaphore: Semaphore,
        val triggeredTaskIdRef: AtomicReference<String?>
    ) {
        fun reset() {
            semaphore.acquire(semaphore.availablePermits())
            triggeredTaskIdRef.set(null)
        }
    }
}