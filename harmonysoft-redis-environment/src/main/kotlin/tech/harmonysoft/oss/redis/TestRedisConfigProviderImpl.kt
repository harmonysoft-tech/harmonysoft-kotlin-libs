package tech.harmonysoft.oss.redis

import jakarta.inject.Named
import org.springframework.beans.factory.ObjectProvider
import tech.harmonysoft.oss.environment.TestEnvironmentManager
import tech.harmonysoft.oss.redis.config.TestRedisConfig
import tech.harmonysoft.oss.redis.config.TestRedisConfigProvider

@Named
class TestRedisConfigProviderImpl(
    private val environmentManager: ObjectProvider<TestEnvironmentManager>,
    private val environment: ObjectProvider<RedisTestEnvironment>
) : TestRedisConfigProvider {

    override fun getData(): TestRedisConfig {
        return environmentManager.getObject().startIfNecessary(environment.getObject())
    }

    override fun refresh() {
    }

    override fun probe(): TestRedisConfig {
        return data
    }
}