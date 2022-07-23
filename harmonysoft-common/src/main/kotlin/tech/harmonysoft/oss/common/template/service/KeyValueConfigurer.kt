package tech.harmonysoft.oss.common.template.service

import tech.harmonysoft.oss.common.data.DataModificationStrategy

/**
 * Stateless modification strategy for any given [DataModificationStrategy]. It can hold static setup like
 * `key1 = value1` and `key2 = value2`, then the data is applied to the given key-values holder.
 *
 * However, there is a possible case that we want to apply rules based on some dynamic runtime state.
 * For example, we receive an event to process and want to create new event based on it. We might define
 * the configuration rules as below:
 *   * if received event's `user' attribute value is equal to `user1` then create new event with `user = user12`
 *   * if received event's `user' attribute value is equal to `user2` then create new event with `user = user22`
 *   * else use `user = user3`
 *
 * Such dynamic behavior is implemented via given context - it holds dynamic info to be consulted during
 * given key-value holder modification. Every implementation of this interface is bound to particular
 * type of context.
 */
interface KeyValueConfigurer<K> {

    /**
     * Set of keys which might be [asked][KeyValueConfigurationContext.getByStaticKey] from the context
     * during [target value configuration][configure] or used as [DataModificationStrategy.setValue] parameter.
     */
    val keys: Set<K>

    /**
     * Shows all possible static configuration which can be done by the current configurer. For example,
     * consider setup like below:
     *
     * ```
     * key1: value1
     * key2: value2
     * ```
     *
     * This property would expose `mapOf("key1" to setOf("value1"), "key2" to setOf("value2"))` then;
     *
     * ```
     * key1:
     *   - When:
     *       key3: value31
     *     Then: value11
     *   - Then: value12
     * key2: value2
     * ```
     *
     * This property would expose `mapOf("key1" to setOf("value11", "value12"), "key2" to setOf("value2"))` then.
     */
    val staticConfiguration: Map<K, Set<Any>>

    fun configure(toConfigure: DataModificationStrategy<K>, context: KeyValueConfigurationContext<K>)

    companion object {

        private val NO_OP = object : KeyValueConfigurer<Any> {

            override val keys = emptySet<Any>()

            override val staticConfiguration = emptyMap<Any, Set<Any>>()

            override fun configure(
                toConfigure: DataModificationStrategy<Any>,
                context: KeyValueConfigurationContext<Any>
            ) {
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun <K> noOp(): KeyValueConfigurer<K> {
            return NO_OP as KeyValueConfigurer<K>
        }
    }
}