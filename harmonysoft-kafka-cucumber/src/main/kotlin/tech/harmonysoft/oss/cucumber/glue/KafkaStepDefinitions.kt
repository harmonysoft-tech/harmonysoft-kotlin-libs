package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.java.en.Given
import javax.inject.Inject
import tech.harmonysoft.oss.kafka.service.TestKafkaManager

class KafkaStepDefinitions {

    @Inject private lateinit var kafka: TestKafkaManager

    @Given("^the following kafka message is sent to topic ([^\\s]+):$")
    fun sendMessage(topic: String, message: String) {
        kafka.sendMessage(topic, message)
    }
}