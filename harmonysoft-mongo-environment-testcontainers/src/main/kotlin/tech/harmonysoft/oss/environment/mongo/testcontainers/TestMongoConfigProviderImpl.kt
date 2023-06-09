package tech.harmonysoft.oss.environment.mongo.testcontainers

import javax.inject.Named
import org.springframework.beans.factory.ObjectProvider
import tech.harmonysoft.oss.environment.TestEnvironmentManager
import tech.harmonysoft.oss.mongo.config.TestMongoConfig
import tech.harmonysoft.oss.mongo.config.TestMongoConfigProvider

@Named
class TestMongoConfigProviderImpl(
    private val environmentManager: ObjectProvider<TestEnvironmentManager>,
    private val environment: ObjectProvider<MongoTestcontainersEnvironment>
) : TestMongoConfigProvider {

    override fun getData(): TestMongoConfig {
        return environmentManager.getObject().startIfNecessary(environment.getObject())
    }

    override fun refresh() {
    }

    override fun probe(): TestMongoConfig {
        return data
    }
}