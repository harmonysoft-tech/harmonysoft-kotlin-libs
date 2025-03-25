package tech.harmonysoft.oss.test.manager

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import jakarta.inject.Named
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.time.util.DateTimeHelper
import tech.harmonysoft.oss.test.TestAware
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.binding.DynamicBindingKey
import tech.harmonysoft.oss.test.content.TestContentManager
import tech.harmonysoft.oss.test.fixture.CommonTestFixture
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.time.clock.TestClockProvider
import tech.harmonysoft.oss.test.util.TestUtil

@Named
class CommonTestManager(
    private val clockProvider: TestClockProvider,
    private val dateTimeHelper: DateTimeHelper,
    private val contentManager: TestContentManager,
    private val fixtureDataHelper: FixtureDataHelper,
    private val bindingContext: DynamicBindingContext
) : TestAware {

    private val _expectTestVerificationFailure = AtomicBoolean()
    val expectTestVerificationFailure: Boolean get() = _expectTestVerificationFailure.get()

    private val _testName = AtomicReference("")
    val activeTestName: String get() = _testName.get()

    private val logger = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    fun setUp(info: TestInfo) {
        setUp(info.displayName)
    }

    fun setUp(testName: String) {
        _testName.set(testName)
        logger.info("starting test '{}'", testName)
    }

    override fun onTestEnd() {
        logger.info("finished test '{}'", activeTestName)
        _expectTestVerificationFailure.set(false)
        _testName.set("")
    }

    fun setTimeZone(zone: String) {
        clockProvider.data.withZone(ZoneId.of(zone))
    }

    fun setTime(time: String) {
        val localTime = dateTimeHelper.parseTime(time)
        val clock = clockProvider.data
        clock.withInstant(
            localTime.atDate(LocalDate.now(clock))
                .atZone(clock.zone)
                .toInstant()
                .toEpochMilli()
        )
    }

    fun setDate(date: String) {
        val clock = clockProvider.data
        val dateTime = dateTimeHelper.parseDateTime("$date 00:00:00.000")
        clock.withInstant(dateTime.atZone(clock.zone).toInstant().toEpochMilli())
    }

    fun setTimeOnDayOfWeek(rawTime: String, rawDayOfWeek: String) {
        val localTime = dateTimeHelper.parseTime(rawTime)
        val zoneId = ZoneId.systemDefault()
        val clock = clockProvider.data
        val today = LocalDate.now(clock)
        val targetDayOfWeek = DayOfWeek.valueOf(rawDayOfWeek.uppercase())
        val daysDiff = today.dayOfWeek.ordinal - targetDayOfWeek.ordinal
        val localDate = LocalDate.now(clock).minusDays(daysDiff.toLong())
        logger.info("USing local date {} and local time {}", localDate, localTime)
        clock.withInstant(
            localTime.atDate(localDate)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()
        )
    }

    fun configureTextContent(name: String, data: String) {
        contentManager.setContent(name, data.toByteArray())
    }

    fun excludeMetaValueFromExpansion(metaValue: String) {
        fixtureDataHelper.excludeMetaValueFromExpansion(metaValue)
    }

    fun bindDynamicValue(key: String, value: String) {
        bindingContext.storeBinding(DynamicBindingKey(key), value)
    }

    fun saveCurrentTime(key: String) {
        bindingContext.storeBinding(DynamicBindingKey(key), System.currentTimeMillis())
    }

    fun verifyDynamicValue(key: String, expected: String) {
        val actual = bindingContext.getBinding(DynamicBindingKey((key)))
        val expectedToUse = fixtureDataHelper.prepareTestData(CommonTestFixture.TYPE, Unit, expected)
        if (actual != expectedToUse) {
            TestUtil.fail("expected dynamic key '$key' to have value '$expected' but it has value '$actual' instead")
        }
    }

    fun verifyDynamicValueIsNotSet(key: String) {
        val dynamicKey = DynamicBindingKey(key)
        val bound = bindingContext.hasBindingFor(dynamicKey)
        if (bound) {
            TestUtil.fail(
                "expected that dynamic key '$key' is not set but it has value '${
                    bindingContext.getBinding(
                        dynamicKey
                    )
                }'"
            )
        }
    }

    fun verifyElapsedTime(expectedDurationMs: Long, startTimeKey: String) {
        val now = System.currentTimeMillis()
        val startTimeMs = (bindingContext.getBinding(DynamicBindingKey(startTimeKey)) as? Long) ?: TestUtil.fail(
            "no start time is stored under dynamic key '$startTimeKey'"
        )
        val actualDuration = now - startTimeMs
        if (actualDuration < expectedDurationMs) {
            TestUtil.fail(
                "expected that at least $expectedDurationMs ms is elapsed since the time anchored by dynamic "
                + "variable '$startTimeKey' ($startTimeMs), but only $actualDuration ms were spent "
                + "(current time is $now)"
            )
        }
    }

    fun expectVerificationFailure() {
        _expectTestVerificationFailure.set(true)
    }
}
