package tech.harmonysoft.oss.common.time.schedule.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tech.harmonysoft.oss.common.time.clock.ClockProvider
import tech.harmonysoft.oss.common.time.schedule.Schedule
import tech.harmonysoft.oss.common.time.util.TimeUtil.Millis
import tech.harmonysoft.oss.test.time.clock.TestClockProvider
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*

internal class ScheduleImplTest {

    enum class Operation {
        CONTAINS, TIME_BEFORE_INTERVAL_END, TIME_BEFORE_INTERVAL_START
    }

    private val monday = LocalDate.of(2019, 12, 30)
    private val clockProvider = TestClockProvider()

    @BeforeEach
    fun setUp() {
        clockProvider.onTestStart()
    }

    @AfterEach
    fun tearDown() {
        clockProvider.onTestEnd()
    }

    private fun <T> doTest(operation: Operation, schedule: Schedule, mondayTime: String, verifier: (T) -> Unit) {
        doTest(
            operation = operation,
            schedule = schedule,
            inputLocalDateTime = dateTime(mondayTime),
            verifier = verifier
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> doTest(
        operation: Operation,
        schedule: Schedule,
        inputLocalDateTime: LocalDateTime,
        verifier: (T) -> Unit
    ) {
        val inputMillis = ZonedDateTime.of(inputLocalDateTime, clockProvider.data.zone)
            .toInstant()
            .toEpochMilli()

        when (operation) {
            Operation.CONTAINS -> {
                verifier(schedule.contains(inputLocalDateTime) as T)
                verifier(schedule.contains(inputMillis) as T)
            }

            Operation.TIME_BEFORE_INTERVAL_END -> {
                verifier(schedule.timeMsUntilTargetTimeIntervalEnd(inputLocalDateTime) as T)
                verifier(schedule.timeMsUntilTargetTimeIntervalEnd(inputMillis) as T)
            }

            Operation.TIME_BEFORE_INTERVAL_START -> {
                verifier(schedule.timeMsUntilTargetTimeIntervalStart(inputLocalDateTime) as T)
                verifier(schedule.timeMsUntilTargetTimeIntervalStart(inputMillis) as T)
            }
        }
    }

    private fun schedule(vararg dayWindows: DaysOfWeekTimeWindows): Schedule {
        return ScheduleImpl(clockProvider, dayWindows.toList())
    }

    private fun windows(daysOfWeek: Set<DayOfWeek>, vararg timeWindows: String): DaysOfWeekTimeWindows {
        return DaysOfWeekTimeWindows(daysOfWeek, timeWindows.map {
            TimeWindow.parse(it)
        }.toSet())
    }

    private fun dateTime(time: String): LocalDateTime {
        return monday.atTime(LocalTime.parse(time))
    }

    @Test
    fun `when overlapping windows are provided in the same entry then they are rejected`() {
        assertThrows<IllegalArgumentException> {
            schedule(windows(MONDAY, "10:00 - 10:02", "10:01 - 10:03"))
        }
    }

    @Test
    fun `when overlapping windows are provided in different entries then they are rejected`() {
        assertThrows<IllegalArgumentException> {
            schedule(
                windows(MONDAY, "11:00 - 11:02"),
                windows(MONDAY, "11:01 - 11:02")
            )
        }
    }

    @Test
    fun `when there is a time window for the target point of time then contains() return true`() {
        val schedule = schedule(
            windows(MONDAY, "10:00 - 10:02", "11:00 - 11:02"),
            windows(MONDAY, "12:00 - 12:02")
        )
        val verifier: (Boolean) -> Unit = { assertThat(it).isTrue() }
        doTest(Operation.CONTAINS, schedule, "10:00", verifier)
        doTest(Operation.CONTAINS, schedule, "10:01", verifier)
        doTest(Operation.CONTAINS, schedule, "10:02", verifier)
        doTest(Operation.CONTAINS, schedule, "11:00", verifier)
        doTest(Operation.CONTAINS, schedule, "11:01", verifier)
        doTest(Operation.CONTAINS, schedule, "11:02", verifier)
        doTest(Operation.CONTAINS, schedule, "12:00", verifier)
        doTest(Operation.CONTAINS, schedule, "12:01", verifier)
        doTest(Operation.CONTAINS, schedule, "12:02", verifier)
    }

    @Test
    fun `when there is no time window for the target point of time then contains() return false`() {
        val schedule = schedule(
            windows(MONDAY, "10:00 - 10:02", "11:00 - 11:02"),
            windows(MONDAY, "12:00 - 12:02")
        )
        val verifier: (Boolean) -> Unit = { assertThat(it).isFalse() }
        doTest(Operation.CONTAINS, schedule, "09:59", verifier)
        doTest(Operation.CONTAINS, schedule, "10:03", verifier)
        doTest(Operation.CONTAINS, schedule, "10:59", verifier)
        doTest(Operation.CONTAINS, schedule, "11:03", verifier)
        doTest(Operation.CONTAINS, schedule, "11:59", verifier)
        doTest(Operation.CONTAINS, schedule, "12:03", verifier)
    }

    @Test
    fun `when target point of time is within a time window then correct duration till time window end is returned`() {
        val schedule = schedule(windows(MONDAY, "10:00 - 10:02"))
        doTest<Long>(Operation.TIME_BEFORE_INTERVAL_END, schedule, "10:00") {
            assertThat(it).isEqualTo(Millis.MINUTE * 2)
        }
        doTest<Long>(Operation.TIME_BEFORE_INTERVAL_END, schedule, "10:01") {
            assertThat(it).isEqualTo(Millis.MINUTE)
        }
        doTest<Long>(Operation.TIME_BEFORE_INTERVAL_END, schedule, "10:02") {
            assertThat(it).isEqualTo(0L)
        }
    }

    @Test
    fun `when various ponts of time are used then contains() works correctly`() {
        val testDate = LocalDate.of(2020, 2, 28)
        val schedule = schedule(windows(WEEKDAYS, "07:30:00 - 21:50:00"))

        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("07:29:59.999999"))) {
            assertThat(it).isFalse()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("07:30:00.000"))) {
            assertThat(it).isTrue()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("07:30:00.000001"))) {
            assertThat(it).isTrue()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("10:02:00.23"))) {
            assertThat(it).isTrue()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("21:50:00.500"))) {
            assertThat(it).isFalse()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("21:51:00.500"))) {
            assertThat(it).isFalse()
        }
    }

    @Test
    fun `when schedule is defined with EOD placeholder then contains() works correctly`() {
        clockProvider.data.withZone(ZoneId.of("Europe/London"))
        val testDate = LocalDate.of(2020, 2, 28)
        val schedule = schedule(windows(WEEKDAYS, "07:30:00 - ${TimeWindow.END_OF_DAY}"))

        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("07:29:59.999999999"))) {
            assertThat(it).isFalse()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("07:30:00.00"))) {
            assertThat(it).isTrue()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("07:30:00.000000000"))) {
            assertThat(it).isTrue()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("07:30:00.000000001"))) {
            assertThat(it).isTrue()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("23:59:59.999999999"))) {
            assertThat(it).isTrue()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("00:00:00.000000000"))) {
            assertThat(it).isFalse()
        }
        doTest<Boolean>(Operation.CONTAINS, schedule, testDate.atTime(LocalTime.parse("00:00:00.000000001"))) {
            assertThat(it).isFalse()
        }
    }

    @Test
    fun `when target point of time is within a time window then correct duration till time window start is returned`() {
        val schedule = schedule(windows(MONDAY, "10:00 - 10:02"))
        val verifier: (Long) -> Unit = { assertThat(it).isNotPositive() }
        doTest(Operation.TIME_BEFORE_INTERVAL_START, schedule, "10:00", verifier)
        doTest(Operation.TIME_BEFORE_INTERVAL_START, schedule, "10:01", verifier)
        doTest(Operation.TIME_BEFORE_INTERVAL_START, schedule, "10:02", verifier)
    }

    @Test
    fun `when target point of time is before a time window on the same day then correct duration till start is returned`() {
        val schedule = schedule(windows(MONDAY, "10:00 - 10:02"))
        doTest<Long>(Operation.TIME_BEFORE_INTERVAL_START, schedule, "09:30") {
            assertThat(it).isEqualTo(Millis.MINUTE * 30)
        }
        doTest<Long>(Operation.TIME_BEFORE_INTERVAL_START, schedule, "00:00") {
            assertThat(it).isEqualTo(Millis.HOUR * 10)
        }
    }

    @Test
    fun `when target point is after a single time window then correct duration till start is returned`() {
        val schedule = schedule(windows(MONDAY, "10:00 - 10:02"))
        doTest<Long>(Operation.TIME_BEFORE_INTERVAL_START, schedule, "11:00") {
            assertThat(it).isEqualTo(Millis.HOUR * 23 + Millis.DAY * 6)
        }
    }

    @Test
    fun `given a single time window which starts at midnight when target point is outside it then correct duration till start is returned`() {
        val schedule = schedule(windows(MONDAY, "00:00 - 10:02"))
        doTest<Long>(Operation.TIME_BEFORE_INTERVAL_START, schedule, "11:00") {
            assertThat(it).isEqualTo(Millis.HOUR * 13 + Millis.DAY * 6)
        }
    }

    @Test
    fun `when no time windows are provided then it's rejected`() {
        assertThrows<IllegalArgumentException> {
            ScheduleImpl(clockProvider, emptyList())
        }
    }

    @Test
    fun `when next time window is tomorrow and now is not beginning of the day then correct time till start is returned`() {
        val schedule = schedule(windows(setOf(DayOfWeek.TUESDAY), "08:00 - 09:00"))

        val dateTime = dateTime("12:00") // Monday
        doTest<Boolean>(Operation.CONTAINS, schedule, dateTime) { assertThat(it).isFalse() }
        doTest<Long>(Operation.TIME_BEFORE_INTERVAL_START, schedule, dateTime) {
            assertThat(it).isEqualTo(Millis.HOUR * 20)
        }
    }

    @Test
    fun `when time zone doesn't use DST then working time is respected`() {
        testWorkingTime(7, 1, 7)
    }

    private fun testWorkingTime(initMonth: Int, initDay: Int, month: Int) {
        val timeZone = TimeZone.getTimeZone("Australia/Sydney")
        val instance = Calendar.getInstance(timeZone)
        instance.set(2020, initMonth, initDay)
        val clockProvider = object : ClockProvider {
            override fun getData(): Clock {
                return Clock.fixed(Instant.ofEpochMilli(instance.timeInMillis), timeZone.toZoneId())
            }

            override fun withZone(zone: ZoneId): ClockProvider {
                return ClockProvider.forZone(zone)
            }

            override fun probe(): Clock {
                return data
            }

            override fun refresh() {
            }
        }
        val schedule = ScheduleImpl(
            clockProvider = clockProvider,
            dayTimeWindows = listOf(
                DaysOfWeekTimeWindows(
                daysOfWeek = DayOfWeek.values().toSet(),
                _timeWindows = setOf(
                    TimeWindow(startTime = LocalTime.MIN, endTime = LocalTime.of(2, 58)),
                    TimeWindow(startTime = LocalTime.of(14, 15), endTime = LocalTime.MAX),
                )
            ))
        )

        instance.set(2020, month, 1, 14, 10)
        assertThat(schedule.contains(instance.timeInMillis)).isFalse()

        instance.set(2020, month, 1, 14, 15)
        assertThat(schedule.contains(instance.timeInMillis)).isTrue()

        instance.set(2020, month, 1, 14, 30)
        assertThat(schedule.contains(instance.timeInMillis)).isTrue()

        instance.set(2020, month, 1, 2, 57)
        assertThat(schedule.contains(instance.timeInMillis)).isTrue()

        instance.set(2020, month, 1, 2, 59)
        assertThat(schedule.contains(instance.timeInMillis)).isFalse()
    }

    @Test
    fun `when time zone uses DST and last monday is not in DST then working time is respected`() {
        testWorkingTime(7, 1, 11)
    }

    @Test
    fun `when time zone uses DST and last monday is in DST then working time is respected`() {
        testWorkingTime(10, 27, 11)
    }

    @Test
    fun `when time zone uses DST and last monday is in DST but now is not then working time is respected`() {
        testWorkingTime(2, 29, 6)
    }

    @Test
    fun `when schedule uses a different time zone then remaining time in millis is correct`() {
        val startTime = "10:10:00"
        val endTime = "10:30:00"

        val laZone = ZoneId.of("America/Los_Angeles")
        clockProvider.data.withZone(laZone)
        val schedule = schedule(windows(WEEKDAYS, "$startTime - $endTime"))

        val dateTime = LocalDate.parse("2021-03-16").atTime(LocalTime.parse(startTime))
        val appZone = ZoneId.of("Asia/Singapore")
        val now = ZonedDateTime.of(dateTime, appZone).toInstant().toEpochMilli()

        val expected = ChronoUnit.MILLIS.between(
            dateTime.atZone(appZone),
            dateTime.atZone(laZone)
        )
        assertThat(schedule.timeMsUntilTargetTimeIntervalStart(now)).isEqualTo(expected)
    }

    companion object {

        val MONDAY = setOf(DayOfWeek.MONDAY)

        val WEEKDAYS = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    }
}