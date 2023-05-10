package tech.harmonysoft.oss.test.match

import tech.harmonysoft.oss.test.binding.DynamicBindingKey
import tech.harmonysoft.oss.test.binding.DynamicBindingContext

/**
 * Holds expected vs actual match results.
 */
data class TestMatchResult(
    /**
     * Mismatch errors (if any). Empty collection means that expected value matched the actual value.
     */
    val errors: Collection<String>,

    /**
     * It might be that expectation has some binding setup. For example, expected JSON might look as below:
     *
     * ```
     * {
     *   "id": <bind:id1>,
     *   "value": 1
     * }
     * ```
     *
     * Let's compare it to actual JSON:
     *
     * ```
     * {
     *   "id": "my-id",
     *   "value": 1
     * }
     * ```
     *
     * It is a match ([errors] is empty) and [boundDynamicValues] has `"id1" to "my-id"`. We introduce this property
     * to correctly handle situations with partial match and eventual mismatch. Example:
     *
     * expectation:
     * ```
     * {
     *   "key1": 1,
     *   "key2": <bind:key2>,
     *   "key3": 3
     * }
     * ```
     *
     * actual:
     * ```
     * {
     *   "key1": 1,
     *   "key2": 2,
     *   "key3": 33
     * }
     * ```
     * Here comparison is performed as below:
     * 1. Compare `key` value -> match
     * 2. Bind `key2` as `2`
     * 3. Compare `key3` -> mismatch
     * We don't want to preserve `key2 = 2` in dynamic context, that's why we return dynamic bindings as detached
     * data structure. It's up to the caller to actually register them in [DynamicBindingContext]
     */
    val boundDynamicValues: Map<DynamicBindingKey, Any?>
)