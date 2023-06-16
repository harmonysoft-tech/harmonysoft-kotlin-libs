package tech.harmonysoft.oss.environment.mongo.testcontainers

import java.util.Optional
import javax.inject.Named
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.environment.mongo.spi.MongoEnvironmentSpi
import tech.harmonysoft.oss.mongo.config.TestMongoConfig

@Named
class TestcontainersMongo(
    ext: Optional<MongoConfigExtension>
) : MongoEnvironmentSpi {

    private val ext = ext.orElse(MongoConfigExtension.Default)

    override val environmentId = "mongo-testcontainers"

    override fun start(context: TestContext): TestMongoConfig {
        val container = GenericContainer(DockerImageName.parse("mongo:6.0.3"))
            .withEnv(mapOf(
                "MONGO_INITDB_ROOT_USERNAME" to "root",
                "MONGO_INITDB_ROOT_PASSWORD" to "root",
                "MONGO_INITDB_DATABASE" to ext.db,
                "MONGO_NON_ROOT_USERNAME" to ext.credential.login,
                "MONGO_NON_ROOT_PASSWORD" to ext.credential.password
            ))
            .withClasspathResourceMapping(
                "/harmonysoft/environment/testcontainers/mongo",
                "/docker-entrypoint-initdb.d",
                BindMode.READ_ONLY
            ).withExposedPorts(27017)
        container.start()
        val port = container.getMappedPort(27017)
        return TestMongoConfig(
            host = "127.0.0.1",
            port = port,
            db = ext.db,
            credential = ext.credential
        )

    }
}