package tech.harmonysoft.oss.environment.mongo.external

import jakarta.inject.Named
import java.util.Optional
import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.environment.mongo.MongoConfigExtension
import tech.harmonysoft.oss.environment.mongo.spi.MongoEnvironmentSpi
import tech.harmonysoft.oss.mongo.config.TestMongoConfig

@Named
class ExternalMongo(
    ext: Optional<MongoConfigExtension>
) : MongoEnvironmentSpi {

    private val ext = ext.orElse(tech.harmonysoft.oss.environment.mongo.MongoConfigExtension.Default)

    override val environmentId = "mongo-external"

    override fun start(context: TestContext): TestMongoConfig {
        return TestMongoConfig(
            host = "127.0.0.1",
            port = 27017,
            db = ext.db,
            credential = ext.credential
        )
    }
}