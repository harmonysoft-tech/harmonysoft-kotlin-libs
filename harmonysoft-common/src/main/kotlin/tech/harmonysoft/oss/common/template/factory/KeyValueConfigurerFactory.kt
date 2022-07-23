package tech.harmonysoft.oss.common.template.factory

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.data.DataModificationStrategy
import tech.harmonysoft.oss.common.data.TypedKeyManager
import tech.harmonysoft.oss.common.type.TypeManagerContext
import tech.harmonysoft.oss.common.type.TypeManager

/**
 * Defines contract for building [KeyValueConfigurer] instances. The main idea is that it receives `Map<String, Any>`
 * which holds configuration rules and builds [KeyValueConfigurer] based on that.
 *
 * **DSL:**
 * For simplicity all examples below show YAML config, however, there is no restriction on where it comes from.
 *
 * ------------------------------------------------------------------------------------------------------------
 *
 * *Unconditional static configuration*
 *
 * ```
 * rules:
 *   key1: value1
 *   key2: value2
 * ```
 *
 * This is the simplest possible configuration - target values are defined for the target keys.
 *
 * ------------------------------------------------------------------------------------------------------------
 *
 * *Conditional static configuration with leaf static rules*
 *
 * ```
 * rules:
 *   key1:
 *     - When:
 *         key2: some-value
 *       Then: value1
 *     - When:
 *         key3: some-other-value
 *       Then: value2
 *     - Then: value3
 * ```
 *
 * 1. If [key2 = some-value][KeyValueConfigurationContext.getByStaticKey] then [key1 is set to value1][DataModificationStrategy.setValue]
 * 2. Else if [key3 = some-other-value][KeyValueConfigurationContext.getByStaticKey] then [key1 is set to value1][DataModificationStrategy.setValue]
 * 3. Else [key1 is set to value3][DataModificationStrategy.setValue]
 *
 * *Note: if there is no final unconditional `'Then'` clause, then `key1` value is not set*
 *
 * ------------------------------------------------------------------------------------------------------------
 *
 * *Composite AND filter*
 *
 * ```
 * rules:
 *   key1:
 *     - When:
 *         key2: some-value
 *       Then: value1
 *     - When:
 *         And:
 *           - key3: some-other-value
 *           - key4: one-more-value
 *       Then: value2
 * ```
 *
 * ------------------------------------------------------------------------------------------------------------
 *
 * *Composite OR filter*
 *
 * ```
 * rules:
 *   key1:
 *     - When:
 *         Or:
 *           - key2: some-value
 *           - And:
 *               key3: some-other-value
 *               key4: one-more-value
 *       Then: value
 * ```
 *
 * 1. If `key2 = some-value` or (`key3 = some-other-value` and `key4 = one-more-value`) then `key1` is set to `value`
 * 2. Else `key1` value is not set/changed
 *
 * *Note: any other filter might be used in AND/OR filters*
 *
 * ------------------------------------------------------------------------------------------------------------
 *
 * *Dynamic value with static key*
 *
 * ```
 * rules:
 *   key1:
 *     - When:
 *         key2: 3
 *       Then: <original-key3>
 * ```
 *
 * 1. If [key2 dynamic value = 3][KeyValueConfigurationContext.getByStaticKey] then `key` is set to [key3 dynamic value][KeyValueConfigurationContext.getByStaticKey]
 * 2. Else `key1` value is not set/changed
 *
 * *Note: here and below all elements in angle brackets (<>) mean something dynamic. Here we use a dedicated
 * `original-` prefix as an indication that dynamic value should be retrieved via target static key*
 *
 * ------------------------------------------------------------------------------------------------------------
 *
 * *Dynamic value with static key shorthand*
 *
 * ```
 * rules:
 *   key1:
 *     - When:
 *         key2: 3
 *       Then: 4
 *     - Then: <original>
 * ```
 *
 * 1. If `key2 = 3` then `key1` is set to `4`
 * 2. Else `key1` is set to `key1 dynamic value`
 *
 * *Note: `key1: <original>` is the same as `key1: <original-key1>`*
 *
 * ------------------------------------------------------------------------------------------------------------
 *
 * *Dynamic value with dynamic key*
 *
 * ```
 * rules:
 *   key1:
 *     - When:
 *         key2: <flavor1>
 *       Then: <flavor2>
 * ```
 *
 * 1. If `key2 = `[dynamic value for dynamic key 'flavor1'][KeyValueConfigurationContext.getByDynamicKey] then `key1` is set to [dynamic value for dynamic key 'flavor2'][KeyValueConfigurationContext.getByDynamicKey]
 * 2. Else `key1` value is not set/changed
 *
 * ------------------------------------------------------------------------------------------------------------
 *
 * *Dynamic key*
 *
 * ```
 * rules:
 *   key1:
 *     - When:
 *         <flow>: flow1
 *       Then: value1
 *     - When:
 *         key2: value2
 *       Then: value3
 * ```
 *
 * 1. If [dynamic value for dynamic key 'flow'][KeyValueConfigurationContext.getByDynamicKey] is equal to `flow`
 *    then `key1` is set to `value1`
 * 2. Else if `key2 = value2` then `key1` is set to `value3`
 * 3. Else `key1` value is not set/changed
 *
 * ------------------------------------------------------------------------------------------------------------
 *
 * *Rich string value with dynamic value by static key*
 *
 * ```
 * rules:
 *   key1:
 *     - When:
 *         key2: 1
 *       Then: "prefix <original-key3> suffix"
 * ```
 *
 * 1. If [key1 type is string][TypedKeyManager.getValueType] and `key2 = 1` then set `key1` value as string
 *    `prefix 'dynamic key3 value' suffix`
 * 2. Else `key1` value is not set/changed
 *
 * ------------------------------------------------------------------------------------------------------------
 *
 * *Rich string value with dynamic value by dynamic key*
 *
 * ```
 * rules:
 *   key1:
 *     - When:
 *         key2: 1
 *       Then: "prefix <flow> suffix"
 * ```
 *
 * 1. If [key1 type is string][TypedKeyManager.getValueType] and `key2 = 1` then set `key1` value as string
 *    `prefix 'dynamic flow value' suffix`
 * 2. Else `key1` value is not set/changed
 */
interface KeyValueConfigurerFactory {

    /**
     * @param rawRules      raw configuration rules
     * @param keyManager    key manager to use
     * @param contexts      contexts to use for selecting target [TypeManager], see [TypeManagerContext] for
     *                      more details
     */
    fun <K> build(
        rawRules: Map<String, Any>,
        keyManager: TypedKeyManager<K>,
        contexts: Set<TypeManagerContext>
    ): KeyValueConfigurer<K>
}