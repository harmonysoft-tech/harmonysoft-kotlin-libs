package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.java.en.Given
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import tech.harmonysoft.oss.kafka.config.TestKafkaConfigProvider
import tech.harmonysoft.oss.kafka.fixture.KafkaFixtureContext
import tech.harmonysoft.oss.kafka.fixture.KafkaTestFixture
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import javax.inject.Inject

class KafkaStepDefinitions {

    @Inject private lateinit var configProvider: TestKafkaConfigProvider
    @Inject private lateinit var fixtureHelper: FixtureDataHelper

    private fun withProducer(action: (KafkaProducer<String, String>) -> Any) {
        val config = configProvider.data
        val producer = KafkaProducer<String, String>(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "${config.host}:${config.port}",
                ProducerConfig.CLIENT_ID_CONFIG to "test",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
            )
        )
        try {
            action(producer)
        } finally {
            producer.close()
        }
    }

    @Given("^the following kafka message is sent to topic ([^\\s]+):$")
    fun sendMessage(topic: String, message: String) {
        withProducer {
            val withExpandedMetaData = fixtureHelper.prepareTestData(
                type = KafkaTestFixture.TYPE,
                context = KafkaFixtureContext(topic),
                data = message
            ).toString()
            it.send(ProducerRecord(topic, withExpandedMetaData))
        }
    }
}