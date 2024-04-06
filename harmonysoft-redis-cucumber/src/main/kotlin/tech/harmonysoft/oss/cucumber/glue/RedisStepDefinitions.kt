package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import javax.inject.Inject
import tech.harmonysoft.oss.redis.TestRedisManager

class RedisStepDefinitions {

    @Inject private lateinit var redis: TestRedisManager

    @Given("^we configure the following record in redis ([^=]+)=([^\\s]+)$")
    fun configureRecord(key: String, value: String) {
        redis.setValue(key, value)
    }

    @Given("^redis value for key '([^']+)' is stored under dynamic key '([^']+)'$")
    fun bindValue(key: String, dynamicKeyToStoreValue: String) {
        redis.bindValue(key, dynamicKeyToStoreValue)
    }

    @Then("^redis should have record ([^=]+)=([^\\s]+)$")
    fun verifyRecord(key: String, value: String) {
        redis.verifyValue(key, value)
    }
}