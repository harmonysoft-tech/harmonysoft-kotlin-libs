package tech.harmonysoft.oss.environment.kafka

import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Named
import org.apache.kafka.common.serialization.StringDeserializer
import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.environment.TestEnvironment
import tech.harmonysoft.oss.environment.kafka.spi.KafkaEnvironmentSpi
import tech.harmonysoft.oss.kafka.config.TestKafkaConfig
import tech.harmonysoft.oss.kafka.service.TestKafkaManager

@Named
class KafkaTestEnvironment(
    private val kafkaManager: TestKafkaManager,
    private val starter: KafkaEnvironmentSpi
) : TestEnvironment<TestKafkaConfig> {

    override val id = starter.environmentId

    override val configClass = TestKafkaConfig::class

    override fun isRunning(config: TestKafkaConfig): Boolean {
        val topic = UUID.randomUUID().toString()
        val message = UUID.randomUUID().toString()

        try {
            kafkaManager.ensureTopicExists(config, topic)
        } catch (e: Exception) {
            return false
        }

        val consumer = kafkaManager.createConsumer(
            topic = topic,
            config = config,
            keyDeserializer = StringDeserializer::class,
            valueDeserializer = StringDeserializer::class
        )
        try {
            kafkaManager.sendMessage(config, topic, message)

            val checkTimeEnd = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(40)
            while (System.currentTimeMillis() < checkTimeEnd) {
                // we do this with timeout because when consumer subscribes to kafka, it takes some time to settle that
                val record = consumer.poll(Duration.ofSeconds(1))
                val received = !record.isEmpty && record.records(topic).any { it.value() == message }
                if (received) {
                    return true
                }
            }
        } finally {
            consumer.close(Duration.ofMillis(500))
        }
        return false
    }

    override fun start(context: TestContext): TestKafkaConfig {
        return starter.start(context)
    }
}