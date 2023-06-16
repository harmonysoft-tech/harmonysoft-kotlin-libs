package tech.harmonysoft.oss.environment.mongo.spi

import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.mongo.config.TestMongoConfig

interface MongoEnvironmentSpi {

    val environmentId: String

    fun start(context: TestContext): TestMongoConfig
}