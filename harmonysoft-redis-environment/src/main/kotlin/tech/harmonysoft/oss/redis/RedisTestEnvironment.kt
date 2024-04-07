package tech.harmonysoft.oss.redis

import jakarta.inject.Named
import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.environment.TestEnvironment
import tech.harmonysoft.oss.redis.config.TestRedisConfig
import tech.harmonysoft.oss.redis.spi.RedisEnvironmentSpi

@Named
class RedisTestEnvironment(
    private val spi: RedisEnvironmentSpi,
    private val manager: TestRedisManager
) : TestEnvironment<TestRedisConfig> {

    override val id = spi.environmentId

    override val configClass = TestRedisConfig::class

    override fun isRunning(config: TestRedisConfig): Boolean {
        return try {
            manager.buildClient(config).get("test")
            true
        } catch (ignore: Exception) {
            false
        }
    }

    override fun start(context: TestContext): TestRedisConfig {
        return spi.start(context)
    }
}