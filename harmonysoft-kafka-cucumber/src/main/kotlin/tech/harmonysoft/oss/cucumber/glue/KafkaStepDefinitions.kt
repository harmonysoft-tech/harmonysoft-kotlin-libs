package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.java.After
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import javax.inject.Inject
import tech.harmonysoft.oss.kafka.service.TestKafkaManager

class KafkaStepDefinitions {

    @Inject private lateinit var kafka: TestKafkaManager

    @After
    fun tearDown() {
        kafka.tearDown()
    }

    @Given("^kafka topic '$([^']+)' exists$")
    fun createTopicIfNecessary(name: String) {
        kafka.ensureTopicExists(name)
    }

    @Given("^kafka topic '([^']+)' is subscribed$")
    fun subscribe(topic: String) {
        kafka.subscribe(topic)
    }

    @Given("^header ([^=]+)=([^\\s]+) is used for sending all subsequent kafka messages$")
    fun addHeader(key: String, value: String) {
        kafka.addHeader(key, value)
    }

    @Given("^header ([^\\s]+) is not used for sending all subsequent kafka messages$")
    fun cleanHeader(key: String) {
        kafka.cleanHeader(key)
    }

    @Given("^all kafka message headers are reset$")
    fun cleanAllHeaders() {
        kafka.cleanAllHeaders()
    }

    @Given("^the following kafka message is sent to topic '([^']+)':$")
    fun sendMessage(topic: String, message: String) {
        kafka.sendMessage(topic, message)
    }

    @Then("^the following message is received in kafka topic '([^']+)':$")
    fun verifyMessageIsReceived(topic: String, expected: String) {
        kafka.verifyMessageIsReceived(expected, topic)
    }

    @Then("^a message with header ([^=]+)=([^\\s]+) is received in kafka topic '([^']+)'$")
    fun verifyMessageWithHeaderValueIsReceived(key: String, value: String, topic: String) {
        kafka.verifyMessageWithTargetHeaderValueIsReceived(
            topic = topic,
            headerKey = key,
            expectedHeaderValue = value
        )
    }

    @Then("^a JSON message with at least the following data is received in kafka topic '([^']+)':$")
    fun verifyJsonMessageIsReceived(topic: String, expectedJson: String) {
        kafka.verifyJsonMessageIsReceived(expectedJson, topic)
    }

    @Then("^the following message is not received in kafka topic '([^']+)':$")
    fun verifyNoMessageIsReceived(topic: String, expected: String) {
        kafka.verifyMessageIsNotReceived(expected, topic)
    }

    @Then("^a JSON message with at least the following data is not received in kafka topic '([^']+)':$")
    fun verifyNoJsonMessageIsReceived(topic: String, expected: String) {
        kafka.verifyJsonMessageIsNotReceived(expected, topic)
    }

    @Then("^no message is received in kafka topic '([^']+)':$")
    fun verifyNoMessageIsReceived(topic: String) {
        kafka.verifyNoMessageIsReceived(topic)
    }
}