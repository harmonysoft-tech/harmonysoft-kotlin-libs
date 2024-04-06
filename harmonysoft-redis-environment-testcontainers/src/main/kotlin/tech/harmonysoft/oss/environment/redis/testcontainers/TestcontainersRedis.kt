package tech.harmonysoft.oss.environment.redis.testcontainers

import javax.inject.Named
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import tech.harmonysoft.oss.environment.TestContext
import tech.harmonysoft.oss.redis.config.TestRedisConfig
import tech.harmonysoft.oss.redis.spi.RedisEnvironmentSpi

@Named
class TestcontainersRedis : RedisEnvironmentSpi {

    override val environmentId = "redis-testcontainers"

    override fun start(context: TestContext): TestRedisConfig {
        val container = GenericContainer(DockerImageName.parse("redis:7.2.4"))
            .withExposedPorts(6379)
        container.start()
        val port = container.getMappedPort(6379)
        return TestRedisConfig(
            host = "127.0.0.1",
            port = port
        )
    }
}