package tech.harmonysoft.oss.kafka.config

import tech.harmonysoft.oss.configurario.client.ConfigProvider

interface TestKafkaConfigProvider : ConfigProvider<TestKafkaConfig>

data class TestKafkaConfig(
    val host: String,
    val port: Int
)