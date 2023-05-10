package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.time.util.DateTimeHelper
import tech.harmonysoft.oss.test.TestAware
import tech.harmonysoft.oss.test.content.TestContentManager
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.time.clock.TestClockProvider
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.binding.DynamicBindingKey
import tech.harmonysoft.oss.test.util.TestUtil.fail

class CommonStepDefinitions {

    private val logger = LoggerFactory.getLogger(CommonStepDefinitions::class.java)

    @Inject private lateinit var testCallbacks: Optional<Collection<TestAware>>
    @Inject private lateinit var clockProvider: TestClockProvider
    @Inject private lateinit var dateTimeHelper: DateTimeHelper
    @Inject private lateinit var contentManager: TestContentManager
    @Inject private lateinit var fixtureDataHelper: FixtureDataHelper
    @Inject private lateinit var bindingContext: DynamicBindingContext

    @Before
    fun logScenarioStart(scenario: Scenario) {
        logger.info("Starting scenario '{}'", scenario.name)
    }

    @After
    fun logScenarioEnd(scenario: Scenario) {
        logger.info("Finished scenario '{}'", scenario.name)
    }

    @Before
    fun notifyOnTestStart() {
        testCallbacks.ifPresent {
            for (callback in it) {
                callback.onTestStart()
            }
        }
    }

    @After
    fun notifyOnTestEnd() {
        testCallbacks.ifPresent {
            for (callback in it) {
                callback.onTestEnd()
            }
        }
    }

    @Given("^current time zone is set as ([^\\s]+)$")
    fun setTimeZone(zoneId: String) {
        val zone = ZoneId.of(zoneId)
        clockProvider.data.withZone(zone)
    }

    @Given("^current time is set as ([^\\s]+)$")
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

    @Given("^current date is set as ([^\\s]+)$")
    fun setDate(date: String) {
        val clock = clockProvider.data
        val dateTime = dateTimeHelper.parseDateTime("$date 00:00:00.000")
        clock.withInstant(dateTime.atZone(clock.zone).toInstant().toEpochMilli())
    }

    @Given("^current date/time is set as ([^\\s]+) ([^\\s]+) ([^\\s]+)$")
    fun setDateTimeZone(date: String, time: String, zone: String) {
        setTimeZone(zone)
        setDate(date)
        setTimeZone(time)
    }

    @Given("^current time is set as ([^\\s]+) on ([^\\s]+)$")
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

    @Given("^the application sleeps (\\d+) seconds$")
    fun sleep(timeToSleepInSeconds: Long) {
        Thread.sleep(TimeUnit.SECONDS.toMillis(timeToSleepInSeconds))
    }

    @Given("^the following text content with name '([^']+)' is prepared:$")
    fun configureTextContent(name: String, data: String) {
        contentManager.setContent(name, data.toByteArray())
    }

    @Given("^meta-value <([^>]+)> is excluded from auto expansion$")
    fun excludeMetaValueFromExpansion(metaValue: String) {
        fixtureDataHelper.excludeMetaValueFromExpansion(metaValue)
    }

    @Given("^dynamic key ([^\\s]+) is bound to value '([^']+)'$")
    fun bindDynamicValue(key: String, value: String) {
        bindingContext.storeBinding(DynamicBindingKey(key), value)
    }

    @Then("^dynamic key '([^']+)' should have value '([^']+)'$")
    fun verifyDynamicValue(key: String, expected: String) {
        val actual = bindingContext.getBinding(DynamicBindingKey((key)))
        if (actual != expected) {
            fail("expected dynamic key '$key' to have value '$expected' but it has value '$actual' instead")
        }
    }

    @Then("^dynamic key '([^']+)' is not set'$")
    fun verifyDynamicValueIsNotSet(key: String) {
        val dynamicKey = DynamicBindingKey(key)
        val bound = bindingContext.hasBindingFor(dynamicKey)
        if (bound) {
            fail("expected that dynamic key '$key' is not set but it has value '${bindingContext.getBinding(dynamicKey)}'")
        }
    }
}