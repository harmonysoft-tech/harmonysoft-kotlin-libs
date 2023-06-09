package tech.harmonysoft.oss.environment.kafka.spi

import tech.harmonysoft.oss.kafka.config.TestKafkaConfig

interface KafkaEnvironmentSpi {

    val environmentId: String

    fun start(): TestKafkaConfig
}