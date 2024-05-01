package tech.harmonysoft.oss.environment.redis.redis

import jakarta.inject.Named
import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.redis.config.TestRedisConfig
import tech.harmonysoft.oss.redis.spi.RedisEnvironmentSpi

@Named
class ExternalRedis : RedisEnvironmentSpi {

    override val environmentId = "redis-external"

    override fun start(context: TestContext): TestRedisConfig {
        return TestRedisConfig(
            host = "127.0.0.1",
            port = 6379
        )
    }
}