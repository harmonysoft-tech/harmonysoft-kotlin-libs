package tech.harmonysoft.oss.environment.mongo.testcontainers

import com.mongodb.client.MongoClient
import java.util.Optional
import javax.inject.Named
import org.junit.jupiter.api.fail
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.MongoDBContainer
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
        val container = MongoDBContainer(DockerImageName.parse("mongo:4.4.21"))
            .withEnv(
                mapOf(
                    "MONGO_INITDB_DATABASE" to ext.db,
                    "MONGO_NON_ROOT_USERNAME" to ext.credential.login,
                    "MONGO_NON_ROOT_PASSWORD" to ext.credential.password
                )
            )
            .withClasspathResourceMapping(
                "/harmonysoft/environment/testcontainers/mongo",
                "/docker-entrypoint-initdb.d",
                BindMode.READ_ONLY
            )
        container.start()
        val connectionString = container.connectionString
        val i = connectionString.lastIndexOf(":")
        if (i <= 0) {
            fail("can not extract mongo port from connection string '$connectionString'")
        }
        val port = connectionString.substring(i + 1).toInt()
        return TestMongoConfig(
            host = "127.0.0.1",
            port = port,
            db = ext.db,
            credential = ext.credential
        )
    }
}