package tech.harmonysoft.oss.redis

import jakarta.inject.Named
import redis.clients.jedis.Jedis
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.redis.config.TestRedisConfig
import tech.harmonysoft.oss.redis.config.TestRedisConfigProvider
import tech.harmonysoft.oss.redis.fixture.RedisTestFixture
import tech.harmonysoft.oss.test.TestAware
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.binding.DynamicBindingKey
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.util.VerificationUtil

@Named
class TestRedisManager(
    private val configProvider: TestRedisConfigProvider,
    private val fixtureHelper: FixtureDataHelper,
    private val bindingContext: DynamicBindingContext
) : TestAware {

    val client: Jedis get() = buildClient(configProvider.data)

    fun buildClient(config: TestRedisConfig): Jedis {
        return Jedis(config.host, config.port)
    }

    override fun onTestEnd() {
        client.flushAll()
    }

    fun getValue(key: String): String? {
        val expandedKey = fixtureHelper.prepareTestData(
            type = RedisTestFixture.TYPE,
            context = Any(),
            data = key
        ).toString()
        return client.get(expandedKey)
    }

    fun setValue(key: String, value: String) {
        val expandedKey = fixtureHelper.prepareTestData(
            type = RedisTestFixture.TYPE,
            context = Any(),
            data = key
        ).toString()
        val expandedValue = fixtureHelper.prepareTestData(
            type = RedisTestFixture.TYPE,
            context = Any(),
            data = value
        ).toString()
        client.set(expandedKey, expandedValue)
    }

    fun resetValue(key: String) {
        val expandedKey = fixtureHelper.prepareTestData(
            type = RedisTestFixture.TYPE,
            context = Any(),
            data = key
        ).toString()
        client.del(expandedKey)
    }

    fun bindValue(key: String, dynamicKeyToStoreValue: String) {
        val value = getValue(key)
        bindingContext.storeBinding(DynamicBindingKey(dynamicKeyToStoreValue), value)
    }

    fun verifyValue(key: String, expectedValue: String) {
        val expandedKey = fixtureHelper.prepareTestData(
            type = RedisTestFixture.TYPE,
            context = Any(),
            data = key
        ).toString()
        val expandedExpectedValue = fixtureHelper.prepareTestData(
            type = RedisTestFixture.TYPE,
            context = Any(),
            data = expectedValue
        ).toString()
        VerificationUtil.verifyConditionHappens("redis has $expandedKey=$expandedExpectedValue") {
            val actualValue = getValue(expandedKey)
            if (actualValue == expandedExpectedValue) {
                ProcessingResult.success()
            } else {
                ProcessingResult.failure("unexpected value '$actualValue' is found")
            }
        }
    }
}