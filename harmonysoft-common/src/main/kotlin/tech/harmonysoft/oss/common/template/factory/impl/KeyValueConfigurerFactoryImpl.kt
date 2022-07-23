package tech.harmonysoft.oss.common.template.factory.impl

import tech.harmonysoft.oss.common.data.TypedKeyManager
import tech.harmonysoft.oss.common.meta.MetaValueUtil.DYNAMIC_VALUE_PATTERN
import tech.harmonysoft.oss.common.template.factory.KeyValueConfigurerFactory
import tech.harmonysoft.oss.common.template.factory.impl.ConfigurerConstants.ORIGINAL_PREFIX
import tech.harmonysoft.oss.common.template.factory.impl.ConfigurerConstants.ORIGINAL_SHORTHAND
import tech.harmonysoft.oss.common.template.factory.impl.ConfigurerConstants.THEN
import tech.harmonysoft.oss.common.template.factory.impl.ConfigurerConstants.WHEN
import tech.harmonysoft.oss.common.template.factory.impl.condition.*
import tech.harmonysoft.oss.common.template.factory.impl.configurer.*
import tech.harmonysoft.oss.common.template.factory.impl.provider.DynamicKeyValueProvider
import tech.harmonysoft.oss.common.template.factory.impl.provider.StaticKeyValueProvider
import tech.harmonysoft.oss.common.template.factory.impl.provider.StaticValueProvider
import tech.harmonysoft.oss.common.template.factory.impl.provider.ValueProvider
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer
import tech.harmonysoft.oss.common.type.TypeManagerContext
import tech.harmonysoft.oss.common.type.TypeManagersHelper
import javax.inject.Named

@Named
class KeyValueConfigurerFactoryImpl(
    private val typeManagersHelper: TypeManagersHelper
) : KeyValueConfigurerFactory {

    override fun <K> build(
        rawRules: Map<String, Any>,
        keyManager: TypedKeyManager<K>,
        contexts: Set<TypeManagerContext>
    ): KeyValueConfigurer<K> {
        val configurers = rawRules.map { (key, value) ->
            parseConfigurer(key, value, keyManager, contexts)
        }
        return if (configurers.size == 1) {
            configurers.first()
        } else {
            CompositeKeyValueConfigurer(configurers.toTypedArray())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K> parseConfigurer(
        key: String,
        value: Any,
        keyManager: TypedKeyManager<K>,
        contexts: Set<TypeManagerContext>
    ): KeyValueConfigurer<K> {
        return when (value) {
            is String -> parseLeafConfigurer(key, value, keyManager, contexts)
            is List<*> -> CompositeConditionalKeyValueConfigurer(
                value.map { entry ->
                    if (entry is Map<*, *>) {
                        parseConditionalConfigurer(key, entry as Map<String, Any>, keyManager, contexts)
                    } else {
                        throw IllegalArgumentException(
                            "failed to parse conditional rule for key '$key' and list value $value - it's expected "
                            + "to be a map but got ${entry?.let { it::class.qualifiedName }} ($entry)"
                        )
                    }
                }.toTypedArray()
            )
            else -> throw IllegalArgumentException(
                "unexpected value type (${value::class.qualifiedName}) for key '$key': $value"
            )
        }
    }

    private fun <K> parseLeafConfigurer(
        key: String,
        value: String,
        keyManager: TypedKeyManager<K>,
        contexts: Set<TypeManagerContext>
    ): KeyValueConfigurer<K> {
        val typedKey = keyManager.parseKey(key)
        val valueType = keyManager.getValueType(typedKey)
        val valueTypeManager = typeManagersHelper.getTypeManager(valueType, contexts)
        val match = DYNAMIC_VALUE_PATTERN.matchEntire(value)
                    ?: return DYNAMIC_VALUE_PATTERN.find(value)?.let {
                        parseRichStringConfigurer(typedKey, value, keyManager)
                    } ?: StaticLeafKeyValueConfigurer(typedKey, valueTypeManager.maybeParse(value))
        val dynamicKey = match.groupValues[1]
        return when {
            dynamicKey == ORIGINAL_SHORTHAND -> DynamicLeafKeyValueConfigurerWithStaticKey(typedKey, typedKey)
            dynamicKey.startsWith(ORIGINAL_PREFIX) -> DynamicLeafKeyValueConfigurerWithStaticKey(
                typedKey, keyManager.parseKey(dynamicKey.substring(ORIGINAL_PREFIX.length))
            )
            else -> DynamicLeafKeyValueConfigurerWithDynamicKey(typedKey, dynamicKey)
        }
    }

    private fun <K> parseRichStringConfigurer(
        key: K,
        value: String,
        keyManager: TypedKeyManager<K>
    ): KeyValueConfigurer<K> {
        val providers = mutableListOf<ValueProvider<K>>()
        var index = 0
        while (index >= 0 && index < value.length) {
            val match = DYNAMIC_VALUE_PATTERN.find(value, index)
            if (match == null) {
                providers += StaticValueProvider(value.substring(index))
                break
            }

            if (match.range.first > index) {
                providers += StaticValueProvider(value.substring(index, match.range.first))
            }

            val dynamicKey = match.groupValues[1]
            val provider: ValueProvider<K> = when {
                dynamicKey == ORIGINAL_SHORTHAND -> StaticKeyValueProvider(key)
                dynamicKey.startsWith(ORIGINAL_PREFIX) -> StaticKeyValueProvider(keyManager.parseKey(
                    dynamicKey.substring(ORIGINAL_PREFIX.length)
                ))
                else -> DynamicKeyValueProvider(dynamicKey)
            }
            providers += provider
            index = match.range.last + 1
        }
        return RichStringKeyValueConfigurer(key, providers)
    }

    private fun <K> parseConditionalConfigurer(
        key: String,
        rules: Map<String, Any>,
        keyManager: TypedKeyManager<K>,
        contexts: Set<TypeManagerContext>
    ): ConditionalConfigurer<K> {
        val value = rules[THEN]?.let { it as String } ?: throw IllegalArgumentException(
            "failed to parse conditional configurer from '$rules' - it doesn't have a mandatory '$THEN' clause"
        )
        val configurer = parseLeafConfigurer(key, value, keyManager, contexts)
        val condition = rules[WHEN]?.let { rawCondition ->
            when (rawCondition) {
                is Map<*, *> -> if (rawCondition.size == 1) {
                    parseCondition(
                        rawCondition.keys.first() as String,
                        rawCondition.values.first() as Any,
                        keyManager,
                        contexts
                    )
                } else {
                    throw IllegalArgumentException(
                        "failed to parse condition from '$rawCondition' - expected it to have a single entry "
                        + "but got ${rawCondition.size}"
                    )
                }

                else -> throw IllegalArgumentException(
                    "failed to parse condition from '$rawCondition' - expected it to be a map but "
                    + "got ${rawCondition::class.qualifiedName}"
                )
            }
        } ?: Condition.matchAll()
        return ConditionalConfigurer(condition, configurer)
    }

    private fun <K> parseCondition(
        key: String,
        value: Any,
        keyManager: TypedKeyManager<K>,
        contexts: Set<TypeManagerContext>
    ): Condition<K> {
        return when {
            key == ConfigurerConstants.AND -> AndCondition(parseConditions(value, keyManager, contexts).toTypedArray())
            key == ConfigurerConstants.OR -> OrCondition(parseConditions(value, keyManager, contexts).toTypedArray())
            value is Collection<*> -> parseValueCollectionCondition(key, value, keyManager, contexts)
            else -> parseLeafCondition(key, value as String, keyManager, contexts)
        }
    }

    private fun <K> parseConditions(
        rawConditions: Any,
        keyManager: TypedKeyManager<K>,
        contexts: Set<TypeManagerContext>
    ): Collection<Condition<K>> {
        if (rawConditions !is List<*>) {
            throw IllegalArgumentException(
                "failed to parse conditions from '$rawConditions' - expected it to be a list "
                + "but it's ${rawConditions::class.qualifiedName}"
            )
        }
        return rawConditions.map { rawCondition ->
            if (rawCondition !is Map<*, *>) {
                throw IllegalArgumentException(
                    "failed to parse nested condition '$rawCondition' - it's expected to be a map "
                    + "but got ${rawCondition?.let { it::class.qualifiedName }}"
                )
            }
            if (rawCondition.size != 1) {
                throw IllegalArgumentException(
                    "failed to parse nested condition '$rawCondition' - it's expected to be a map with a single "
                    + "entry but there are ${rawConditions.size} entries there"
                )
            }
            parseCondition(
                key = rawCondition.keys.first() as String,
                value = rawCondition.values.first() as Any,
                keyManager = keyManager,
                contexts = contexts
            )
        }
    }

    private fun <K> parseValueCollectionCondition(
        key: String,
        value: Collection<*>,
        keyManager: TypedKeyManager<K>,
        contexts: Set<TypeManagerContext>
    ): Condition<K> {
        val typedKey = keyManager.parseKey(key)
        val valueType = keyManager.getValueType(typedKey)
        val valueTypeManager = typeManagersHelper.getTypeManager(valueType, contexts)
        return OrCondition(
            value.map {
                StaticKeyStaticValueCondition(typedKey, valueTypeManager.maybeParse(it.toString()))
            }.toTypedArray()
        )
    }

    private fun <K> parseLeafCondition(
        key: String,
        value: String,
        keyManager: TypedKeyManager<K>,
        contexts: Set<TypeManagerContext>
    ): Condition<K> {
        val keyMatch = DYNAMIC_VALUE_PATTERN.matchEntire(key)
        val valueMatch = DYNAMIC_VALUE_PATTERN.matchEntire(value)
        return when {
            keyMatch == null && valueMatch == null -> keyManager.parseKey(key).let {
                val valueType = keyManager.getValueType(it)
                val valueTypeManager = typeManagersHelper.getTypeManager(valueType, contexts)
                StaticKeyStaticValueCondition(it, valueTypeManager.maybeParse(value))
            }

            keyMatch == null && valueMatch != null -> {
                val typedKey = keyManager.parseKey(key)
                val dynamicKey = valueMatch.groupValues[1]
                when {
                    dynamicKey.startsWith(ORIGINAL_PREFIX) -> StaticKeyStaticValueCondition(
                        typedKey,
                        keyManager.parseKey(dynamicKey.substring(ORIGINAL_PREFIX.length))
                    )
                    else -> StaticKeyDynamicValueKeyCondition(typedKey, dynamicKey)
                }
            }

            keyMatch != null && valueMatch == null -> DynamicKeyStaticValueCondition(
                keyMatch.groupValues[1],
                value
            )

            keyMatch != null && valueMatch != null -> DynamicKeyDynamicValueCondition(
                keyMatch.groupValues[1],
                valueMatch.groupValues[1]
            )

            else -> throw IllegalStateException(
                "kotlin compiler fails to ensure that all cases are covered"
            )
        }
    }
}