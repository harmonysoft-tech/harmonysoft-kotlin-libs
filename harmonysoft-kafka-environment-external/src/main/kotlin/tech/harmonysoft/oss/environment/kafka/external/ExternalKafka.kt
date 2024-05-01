package tech.harmonysoft.oss.environment.kafka.external

import jakarta.inject.Named
import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.environment.kafka.spi.KafkaEnvironmentSpi
import tech.harmonysoft.oss.kafka.config.TestKafkaConfig

@Named
class ExternalKafka : KafkaEnvironmentSpi {

    override val environmentId = "kafka-external"

    override fun start(context: TestContext): TestKafkaConfig {
        return TestKafkaConfig("127.0.0.1", 9092)
    }
}