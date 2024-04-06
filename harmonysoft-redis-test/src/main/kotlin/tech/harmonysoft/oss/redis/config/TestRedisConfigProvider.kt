package tech.harmonysoft.oss.redis.config

import tech.harmonysoft.oss.inpertio.client.ConfigProvider

interface TestRedisConfigProvider : ConfigProvider<TestRedisConfig>

data class TestRedisConfig(
    val host: String,
    val port: Int
)