package tech.harmonysoft.oss.environment.mongo.testcontainers

import com.mongodb.client.MongoClient
import java.util.Optional
import javax.inject.Named
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.environment.TestEnvironment
import tech.harmonysoft.oss.mongo.config.TestMongoConfig
import tech.harmonysoft.oss.mongo.service.TestMongoManager

@Named
class MongoTestcontainersEnvironment(
    private val manager: TestMongoManager,
    ext: Optional<MongoConfigExtension>
) : TestEnvironment<TestMongoConfig> {

    private val ext = ext.orElse(MongoConfigExtension.Default)

    override val id = "mongo-testcontainers"

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