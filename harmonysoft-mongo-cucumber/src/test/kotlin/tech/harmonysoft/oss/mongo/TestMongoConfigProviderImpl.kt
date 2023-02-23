package tech.harmonysoft.oss.mongo

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Named
import tech.harmonysoft.oss.mongo.config.TestMongoConfig
import tech.harmonysoft.oss.mongo.config.TestMongoConfigProvider

@Named
class TestMongoConfigProviderImpl : TestMongoConfigProvider {

    private val port = AtomicInteger()

    fun setPort(port: Int) {
        this.port.set(port)
    }

    override fun getData(): TestMongoConfig {
        return TestMongoConfig(
            host = "localhost",
            port = port.get(),
            db = "test",
            credential = null
        )
    }

    override fun refresh() {
    }

    override fun probe(): TestMongoConfig {
        return data
    }
}