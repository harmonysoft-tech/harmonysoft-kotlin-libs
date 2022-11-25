package tech.harmonysoft.oss.mongo.config

import tech.harmonysoft.oss.common.auth.model.Credential
import tech.harmonysoft.oss.inpertio.client.ConfigProvider

interface TestMongoConfigProvider : ConfigProvider<TestMongoConfig>

data class TestMongoConfig(
    val host: String,
    val port: Int,
    val db: String,
    val credential: Credential?
)