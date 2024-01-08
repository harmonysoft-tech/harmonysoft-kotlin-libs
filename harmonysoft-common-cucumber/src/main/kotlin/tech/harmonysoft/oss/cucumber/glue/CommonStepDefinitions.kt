package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.test.manager.CommonTestManager

class CommonStepDefinitions {

    private val logger = LoggerFactory.getLogger(CommonStepDefinitions::class.java)

    @Inject private lateinit var manager: CommonTestManager

    @Before
    fun setUp(scenario: Scenario) {
        manager.setUp(scenario.name)
    }

    @After
    fun tearDown() {
        manager.tearDown()
    }

    @Given("^current time zone is set as ([^\\s]+)$")
    fun setTimeZone(zoneId: String) {
        manager.setTimeZone(zoneId)
    }

    @Given("^current time is set as ([^\\s]+)$")
    fun setTime(time: String) {
        manager.setTime(time)
    }

    @Given("^current date is set as ([^\\s]+)$")
    fun setDate(date: String) {
        manager.setDate(date)
    }

    @Given("^current date/time is set as ([^\\s]+) ([^\\s]+) ([^\\s]+)$")
    fun setDateTimeZone(date: String, time: String, zone: String) {
        manager.setDate(date)
        manager.setTime(time)
        manager.setTimeZone(zone)
    }

    @Given("^current time is set as ([^\\s]+) on ([^\\s]+)$")
    fun setTimeOnDayOfWeek(rawTime: String, rawDayOfWeek: String) {
        manager.setTimeOnDayOfWeek(rawTime, rawDayOfWeek)
    }

    @Given("^the application sleeps (\\d+) seconds$")
    fun sleep(timeToSleepInSeconds: Long) {
        Thread.sleep(TimeUnit.SECONDS.toMillis(timeToSleepInSeconds))
    }

    @Given("^the following text content with name '([^']+)' is prepared:$")
    fun configureTextContent(name: String, data: String) {
        manager.configureTextContent(name, data)
    }

    @Given("^meta-value <([^>]+)> is excluded from auto expansion$")
    fun excludeMetaValueFromExpansion(metaValue: String) {
        manager.excludeMetaValueFromExpansion(metaValue)
    }

    @Given("^dynamic key ([^\\s]+) is bound to value '([^']+)'$")
    fun bindDynamicValue(key: String, value: String) {
        manager.bindDynamicValue(key, value)
    }

    @Given("^current time is saved in key '([^']+)'$")
    fun saveCurrentTime(key: String) {
        manager.saveCurrentTime(key)
    }

    @Then("^dynamic key '([^']+)' should have value '([^']+)'$")
    fun verifyDynamicValue(key: String, expected: String) {
        manager.verifyDynamicValue(key, expected)
    }

    @Then("^dynamic key '([^']+)' is not set'$")
    fun verifyDynamicValueIsNotSet(key: String) {
        manager.verifyDynamicValueIsNotSet(key)
    }

    @Then("^at least (\\d+) ms is elapsed since the time anchored by '([^']+)'$")
    fun verifyElapsedTime(expectedDurationMs: Long, startTimeKey: String) {
        manager.verifyElapsedTime(expectedDurationMs, startTimeKey)
    }

    @Given("next test verification is expected to fail")
    fun expectVerificationFailure() {
        manager.expectVerificationFailure()
    }
}