package tech.harmonysoft.oss.environment.kafka

import jakarta.inject.Named
import org.springframework.beans.factory.ObjectProvider
import tech.harmonysoft.oss.environment.TestEnvironmentManager
import tech.harmonysoft.oss.kafka.config.TestKafkaConfig
import tech.harmonysoft.oss.kafka.config.TestKafkaConfigProvider

@Named
class TestKafkaConfigProviderImpl(
    private val environmentManager: ObjectProvider<TestEnvironmentManager>,
    private val environment: ObjectProvider<KafkaTestEnvironment>
) : TestKafkaConfigProvider {

    override fun getData(): TestKafkaConfig {
        return environmentManager.getObject().startIfNecessary(environment.getObject())
    }

    override fun refresh() {
    }

    override fun probe(): TestKafkaConfig {
        return data
    }
}