package tech.harmonysoft.oss.common.template.factory.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import tech.harmonysoft.oss.common.data.DataModificationStrategy
import tech.harmonysoft.oss.common.data.TypedKeyManager
import tech.harmonysoft.oss.common.di.CommonTestConfig
import tech.harmonysoft.oss.common.spring.AbstractSpringTest
import tech.harmonysoft.oss.common.template.factory.KeyValueConfigurerFactory
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer
import tech.harmonysoft.oss.common.type.TypeManagerContext
import kotlin.reflect.KClass

@ContextConfiguration(classes = [CommonTestConfig::class])
internal class KeyValueConfigurerFactoryImplTest : AbstractSpringTest() {

    @Autowired private lateinit var factory: KeyValueConfigurerFactory

    private val mapper = ObjectMapper(YAMLFactory())

    private val stringKeyManager = object : TypedKeyManager<String> {

        override fun getValueType(key: String): KClass<*> {
            return String::class
        }

        override fun parseKey(raw: String): String {
            return raw
        }
    }

    private val keyTypes = mutableMapOf<Any, KClass<*>>()
    private val dynamicValuesByStaticKey = mutableMapOf<Any, Any>()
    private val dynamicValuesByDynamicKey = mutableMapOf<String, Any>()
    private val context = object : KeyValueConfigurationContext<Any> {
        override fun getByStaticKey(key: Any): Any? {
            return dynamicValuesByStaticKey[key]
        }

        override fun getByDynamicKey(key: String): Any? {
            return dynamicValuesByDynamicKey[key]
        }
    }

    private val data = mutableMapOf<Any, Any?>()
    private val dataHolder = object : DataModificationStrategy<Any> {
        override fun setValue(key: Any, value: Any?) {
            data[key] = value
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <K> context(): KeyValueConfigurationContext<K> {
        return context as KeyValueConfigurationContext<K>
    }

    @Suppress("UNCHECKED_CAST")
    fun <K> dataHolder(): DataModificationStrategy<K> {
        return dataHolder as DataModificationStrategy<K>
    }

    @BeforeEach
    fun setUp() {
        dynamicValuesByStaticKey.clear()
        dynamicValuesByDynamicKey.clear()
        keyTypes.clear()
        data.clear()
    }

    private fun configurer(rules: String): KeyValueConfigurer<String> {
        return configurer(rules, stringKeyManager)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K> configurer(
        rules: String,
        keyManager: TypedKeyManager<K>
    ): KeyValueConfigurer<K> {
        val parsedRules = mapper.readValue(rules, MutableMap::class.java) as Map<String, Any>
        return factory.build(parsedRules, keyManager, setOf(TypeManagerContext.DEFAULT))
    }

    @Test
    fun `when unconditional static configuration is used then it works as expected`() {
        val configurer = configurer("""
            key1: '1'
            key2: '2'
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1", "key2")
        assertThat(configurer.staticConfiguration).isEqualTo(mapOf("key1" to setOf("1"), "key2" to setOf("2")))

        data += mapOf("key1" to "11", "key3" to "3")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "1", "key2" to "2", "key3" to "3"))
    }

    @Test
    fun `when conditional static configuration without default clause is used then it works as expected`() {
        val configurer = configurer("""
            key1:
              - When:
                  key4: some-value
                Then: value1
            key2:
              - When:
                  key4: some-other-value
                Then: value2
            key3:
              - When:
                  key4: some-other-value
                Then: value3
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1", "key2", "key3", "key4")
        assertThat(configurer.staticConfiguration).isEqualTo(mapOf(
            "key1" to setOf("value1"),
            "key2" to setOf("value2"),
            "key3" to setOf("value3")
        ))

        data += mapOf("key1" to "1", "key2" to "2")
        dynamicValuesByStaticKey["key4"] = "some-other-value"
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "1", "key2" to "value2", "key3" to "value3"))
    }

    @Test
    fun `when conditional static configuration with default clause is used then it works as expected`() {
        val configurer = configurer("""
            key1:
              - When:
                  key2: some-value
                Then: value1
              - Then: value2
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1", "key2")
        assertThat(configurer.staticConfiguration).isEqualTo(mapOf("key1" to setOf("value1", "value2")))
        dynamicValuesByStaticKey["key2"] = "some-other-value"
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "value2"))
    }

    @Test
    fun `when dynamic value with static key is used then it works as expected`() {
        val configurer = configurer("""
            key1:
              - When:
                  key2: some-value
                Then: <original-key3>
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1", "key2", "key3")
        assertThat(configurer.staticConfiguration).isEmpty()
        dynamicValuesByStaticKey += mapOf("key2" to "some-value", "key3" to "value3")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "value3"))
    }

    @Test
    fun `when dynamic value with shorthand static key is used then it works as expected`() {
        val configurer = configurer("""
            key1:
              - When:
                  key2: some-value
                Then: <original>
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1", "key2")
        assertThat(configurer.staticConfiguration).isEmpty()
        dynamicValuesByStaticKey += mapOf("key1" to "value1", "key2" to "some-value")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "value1"))
    }

    @Test
    fun `when dynamic value with dynamic key is used then it works as expected`() {
        val configurer = configurer("""
            key1:
              - When:
                  key2: <dynamic-key1>
                Then: <dynamic-key2>
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1", "key2")
        assertThat(configurer.staticConfiguration).isEmpty()
        dynamicValuesByStaticKey += mapOf("key2" to "some-value")
        dynamicValuesByDynamicKey += mapOf("dynamic-key1" to "some-value", "dynamic-key2" to "value2")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "value2"))
    }

    @Test
    fun `when dynamic key with static value is used then it works as expected`() {
        val configurer = configurer("""
            key1:
              - When:
                  <flow>: flow1
                Then: value1
              - When:
                  <flow>: flow2
                Then: value2
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1")
        assertThat(configurer.staticConfiguration).isEqualTo(mapOf("key1" to setOf("value1", "value2")))
        dynamicValuesByDynamicKey += mapOf("flow" to "flow2")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "value2"))
    }

    @Test
    fun `when AND is used then it works as expected`() {
        val configurer = configurer("""
            key1:
              - When:
                  And:
                    - key2: value2
                    - key3: value3
                Then: value1
              - When:
                  key2: value2
                Then: value11
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1", "key2", "key3")
        assertThat(configurer.staticConfiguration).isEqualTo(mapOf("key1" to setOf("value1", "value11")))
        dynamicValuesByStaticKey += mapOf("key2" to "value2", "key3" to "some-value")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "value11"))
    }

    @Test
    fun `when OR is used then it works as expected`() {
        val configurer = configurer("""
            key1:
              - When:
                  Or:
                    - key2: value2
                    - key3: value3
                Then: value1
              - Then: value11
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1", "key2", "key3")
        assertThat(configurer.staticConfiguration).isEqualTo(mapOf("key1" to setOf("value1", "value11")))
        dynamicValuesByStaticKey += mapOf("key2" to "value2", "key3" to "value3")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "value1"))
    }

    @Test
    fun `when rich strings with static keys are used then they work as expected`() {
        val configurer = configurer("""
            key1:
              - When:
                  key1: some-value
                Then: "prefix1 <original>"
            key2: "prefix2  <original-key3> suffix2"
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1", "key2", "key3")
        assertThat(configurer.staticConfiguration).isEmpty()
        dynamicValuesByStaticKey += mapOf("key1" to "some-value", "key3" to "value3")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "prefix1 some-value", "key2" to "prefix2  value3 suffix2"))
    }

    @Test
    fun `when rich string with dynamic key is used then it works as expected`() {
        val configurer = configurer("""
            key1:
              - When:
                  key1: some-value
                Then: "prefix1 <flow>"
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1")
        assertThat(configurer.staticConfiguration).isEmpty()
        dynamicValuesByDynamicKey += mapOf("flow" to "my-flow")
        dynamicValuesByStaticKey += mapOf("key1" to "some-value")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "prefix1 my-flow"))
    }

    @Test
    fun `when it's necessary to convert types then it works as expected`() {
        val stringKeys = setOf(69, 70)
        val intKeyManager = object : TypedKeyManager<Int> {
            override fun getValueType(key: Int): KClass<*> {
                return if (key in stringKeys) {
                    String::class
                } else {
                    Int::class
                }
            }

            override fun parseKey(raw: String): Int {
                return raw.toInt()
            }
        }

        val configurer = configurer("""
            1: '1'
            2:
              - When:
                  21: '21'
                Then: '21'
              - Then: '22'
            3:
              - When:
                  31: '31'
                Then: '31'
              - Then: '32'
            4:
              - When:
                  41: '41'
                Then: '41'
              - Then: '42'
            5:
              - When:
                  And:
                    - 21: <i1>
                    - 22: <original-21>
                Then: '51'
              - When:
                  Or:
                    - 21: <i1>
                    - 22: <original-21>
                Then: '52'
            6: <original-31>
            7: <i1>
            8:
              - When:
                  <flow>: f1
                Then: '81'
            69: "prefix[<flow>]suffix"
            70: '70'
        """.trimIndent(), intKeyManager)
        assertThat(configurer.keys).containsOnly(1, 2, 3, 4, 5, 6, 7, 8, 21, 22, 31, 41, 69, 70)
        dynamicValuesByDynamicKey += mapOf("i1" to 221, "flow" to "f1")
        dynamicValuesByStaticKey += mapOf(
            21 to 21,
            22 to 21,
            31 to 33
        )
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf(
            1 to 1,
            2 to 21,
            3 to 32,
            4 to 42,
            5 to 52,
            6 to 33,
            7 to 221,
            8 to 81,
            69 to "prefix[f1]suffix",
            70 to "70"
        ))
    }

    @Test
    fun `when list filter value is used then it is applied`() {
        val configurer = configurer("""
            key1:
              - When:
                  key1: [key11, key12]
                Then: value1
              - Then: value2
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1")
        assertThat(configurer.staticConfiguration).isEqualTo(mapOf("key1" to setOf("value1", "value2")))
        dynamicValuesByStaticKey += mapOf("key1" to "key12")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "value1"))
    }

    @Test
    fun `when list filter value is used and not matched then it's passed through`() {
        val configurer = configurer("""
            key1:
              - When:
                  key1: [key11, key12]
                Then: value1
              - Then: value2
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1")
        assertThat(configurer.staticConfiguration).isEqualTo(mapOf("key1" to setOf("value1", "value2")))
        dynamicValuesByStaticKey += mapOf("key1" to "key2")
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "value2"))
    }

    @Test
    fun `when list filter value is used and there is no value in context then it is passed through`() {
        val configurer = configurer("""
            key1:
              - When:
                  key1: [key11, key12]
                Then: value1
              - Then: value2
        """.trimIndent())
        assertThat(configurer.keys).containsOnly("key1")
        assertThat(configurer.staticConfiguration).isEqualTo(mapOf("key1" to setOf("value1", "value2")))
        configurer.configure(dataHolder(), context())
        assertThat(data).isEqualTo(mapOf("key1" to "value2"))
    }
}