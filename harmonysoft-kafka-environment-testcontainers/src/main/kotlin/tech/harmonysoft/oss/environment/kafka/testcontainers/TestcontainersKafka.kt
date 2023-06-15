package tech.harmonysoft.oss.environment.kafka.testcontainers

import javax.inject.Named
import org.slf4j.Logger
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import tech.harmonysoft.oss.common.environment.EnvironmentUtil
import tech.harmonysoft.oss.environment.kafka.spi.KafkaEnvironmentSpi
import tech.harmonysoft.oss.kafka.config.TestKafkaConfig
import tech.harmonysoft.oss.test.util.TestUtil

@Named
class TestcontainersKafka(
    private val logger: Logger
) : KafkaEnvironmentSpi {

    private val imageToUse: String
        get() {
            // we encountered that regular kafka image doesn't work good under apple silicon - sometimes
            // it fails to start. Confluentinc released dedicated ARM image to mitigate that. That's why
            // here we dynamically choose an image depending on the current environment
            return if (EnvironmentUtil.APPLE_SILICON) {
                logger.info("detected current environment as apple silicon, using corresponding ARM kafka image")
                "confluentinc/cp-kafka:${IMAGE_VERSION}.arm64"
            } else {
                logger.info("detected current environment as non-apple silicon, using regular kafka image")
                "confluentinc/cp-kafka:${IMAGE_VERSION}"
            }
        }

    override val environmentId = "kafka-testcontainers"

    override fun start(): TestKafkaConfig {
        val container = KafkaContainer(DockerImageName.parse(imageToUse))
        logger.info("starting kafka container")
        container.start()
        logger.info("started kafka container")
        val bootstrapServers = container.bootstrapServers
        val i = bootstrapServers.lastIndexOf(":")
        if (i < 0) {
            TestUtil.fail("unsupported kafka bootstrap servers format - '$bootstrapServers'")
        }
        return TestKafkaConfig("127.0.0.1", bootstrapServers.substring(i + 1).toInt())
    }

    companion object {

        const val IMAGE_VERSION = "7.4.0"
    }
}