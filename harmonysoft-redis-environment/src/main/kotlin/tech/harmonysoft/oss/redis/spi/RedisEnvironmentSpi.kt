package tech.harmonysoft.oss.redis.spi

import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.redis.config.TestRedisConfig

interface RedisEnvironmentSpi {

    val environmentId: String

    fun start(context: TestContext): TestRedisConfig
}