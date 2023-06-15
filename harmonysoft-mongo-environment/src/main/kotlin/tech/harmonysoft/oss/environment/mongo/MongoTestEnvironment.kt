package tech.harmonysoft.oss.environment.mongo

import com.mongodb.client.MongoClient
import javax.inject.Named
import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.environment.TestEnvironment
import tech.harmonysoft.oss.environment.mongo.spi.MongoEnvironmentSpi
import tech.harmonysoft.oss.mongo.config.TestMongoConfig
import tech.harmonysoft.oss.mongo.service.TestMongoManager

@Named
class MongoTestEnvironment(
    private val spi: MongoEnvironmentSpi,
    private val manager: TestMongoManager
) : TestEnvironment<TestMongoConfig> {

    override val id = spi.environmentId

    override val configClass = TestMongoConfig::class

    override fun isRunning(config: TestMongoConfig): Boolean {
        var client: MongoClient? = null
        return try {
            client = manager.getClient(config)
            client.listDatabaseNames().toList()
            true
        } catch (_: Exception) {
            false
        } finally {
            try {
                client?.close()
            } catch (ignore: Exception) {
            }
        }
    }

    override fun start(context: TestContext): TestMongoConfig {
        return spi.start()
    }
}