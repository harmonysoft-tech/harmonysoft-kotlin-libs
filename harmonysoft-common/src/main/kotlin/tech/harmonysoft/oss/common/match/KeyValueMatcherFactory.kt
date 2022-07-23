package tech.harmonysoft.oss.common.match

import tech.harmonysoft.oss.common.data.TypedKeyManager
import tech.harmonysoft.oss.common.type.TypeManagerContext
import tech.harmonysoft.oss.common.data.ComparisonStrategy
import tech.harmonysoft.oss.common.type.TypeManager

/**
 * Defines an interface for creating [KeyValueMatcher]
 */
interface KeyValueMatcherFactory<KEY> {

    /**
     * Similar to [build], the only difference is that it should use [ComparisonStrategy] based on
     * [TypeManager.compareTo] chosen with respect to the given contexts
     */
    fun build(
        rule: String,
        keyManager: TypedKeyManager<KEY>,
        vararg contexts: TypeManagerContext
    ): KeyValueMatcher<KEY>

    /**
     * Generally processing logic looks as below:
     * 1. [KeyValueMatcher] works on data holders which can be represented as a bag of key-value pairs
     * 2. Given textual matching rules representations are defined in terms of target keys and their values
     * 3. We parse the given rule representation string and extract string keys from it in factory-specific way,
     *    for example, if it's SQL rule (like `k1 = 1 and (k2 < 2 or k3 in (31, 31)`), we can use sql parser
     *    for that
     * 4. [Parse target keys from their string representations][TypedKeyManager.parseKey]
     * 5. [Get value type for the target key][TypedKeyManager.getValueType]
     * 6. Find [TypeManager] for every value type (see [TypeManager.targetType] and [TypeManager.targetContext]),
     *    given `contexts` allow to customize that
     * 7. [Parse value objects from their string representations given in the rule string][TypeManager.maybeParse]
     * 8. Build [KeyValueMatcher] for the parsed keys, values an operations defined in the given matching
     *    rule string. Given [ComparisonStrategy] is used for that
     */
    fun build(
        rule: String,
        keyManager: TypedKeyManager<KEY>,
        comparison: ComparisonStrategy,
        vararg contexts: TypeManagerContext
    ): KeyValueMatcher<KEY>
}