package tech.harmonysoft.oss.test.json

import tech.harmonysoft.oss.test.binding.DynamicBindingKey
import tech.harmonysoft.oss.test.binding.DynamicBindingUtil.TO_BIND_REGEX
import tech.harmonysoft.oss.test.match.TestMatchResult
import tech.harmonysoft.oss.test.util.TestUtil.fail

object CommonJsonUtil {

    private const val DYNAMIC_VALUE_PREFIX = "dynamic-value-"
    private const val NOT_SET_MARKER = "<not-set>"

    /**
     * Normally when we want to capture any dynamic value from json, we define it as-is, without any quotes:
     *
     * ```
     * {
     *   "key1": <bind:key1>,
     *   "key2": key2-value
     * }
     * ```
     *
     * This is easy to read, but that is malformed json. That's why we normalize it by replacing such dynamic
     * value markers by well-formed json strings starting from [DYNAMIC_VALUE_PREFIX].
     *
     * Example above is transformed to this:
     *
     * ```
     * {
     *   "key1": "dynamic-value-key1",
     *   "key2": key2-value
     * }
     * ```
     */
    fun prepareDynamicMarkers(json: String): String {
        val normalized = StringBuilder()
        var start = 0
        while (true) {
            TO_BIND_REGEX.find(json, start)?.let {
                normalized
                    .append(json.substring(start, it.range.first))
                    .append("\"")
                    .append(DYNAMIC_VALUE_PREFIX)
                    .append(it.groupValues[1])
                    .append("\"")
                start = it.range.last + 1
            } ?: break
        }
        if (start < json.length) {
            normalized.append(json.substring(start))
        }
        return normalized.toString()
    }

    /**
     * There is a common use-case when we want to compare json content. It's also possible that we want to extract
     * dynamic value mappings from it. For example, we might receive the following expected content:
     *
     * ```
     * {
     *   "key1": <bind:key1>,
     *   "key2": key2-value
     * }
     * ```
     *
     * and the following actual content:
     *
     * ```
     * {
     *   "key1": 123,
     *   "key2": key2-value
     * }
     * ```
     *
     * Here we want to do the following:
     * 1. Store dynamic value mapping between `key1` and `123`
     * 2. Return empty comparison errors collection
     *
     * If expected content is like below instead:
     *
     * ```
     * {
     *   "key1": 123,
     *   "key2": other-key2-value
     * }
     * ```
     *
     * we want to return a collection of a single error about non-dynamic value mismatch (`key2-value`
     * vs `other-key2-value`).
     *
     * This method allows to do that
     *
     * @param strict    defines if we should ensure that all data from actual json is present in expected json.
     *                  For example, we can receive a big json response from server but want just to verify that
     *                  particular path has particular value
     * @return collection of data comparison errors (if any); empty collection as an indication of successful comparison
     */
    fun compareAndBind(
        expected: Any,
        actual: Any,
        path: String = "<root>",
        strict: Boolean = true
    ): TestMatchResult {
        if (expected::class != actual::class) {
            return TestMatchResult(
                errors = listOf(
                    "expected an instance of ${expected::class.qualifiedName} ($expected) at path '$path' " +
                    "but got and instance of ${actual::class.qualifiedName} ($actual"
                ),
                boundDynamicValues = emptyMap()
            )
        }
        return when {
            expected is Map<*, *> -> compareAndBindMap(expected, actual, path, strict)

            expected is List<*> -> compareAndBindList(expected, actual, path, strict)

            expected is String && expected.startsWith(DYNAMIC_VALUE_PREFIX) -> {
                TestMatchResult(emptyList(), mapOf(
                    DynamicBindingKey(expected.substring(DYNAMIC_VALUE_PREFIX.length)) to actual
                ))
            }

            else -> if (expected == actual) {
                TestMatchResult(emptyList(), emptyMap())
            } else {
                TestMatchResult(
                    errors = listOf("mismatch at path '$path' - expected a ${expected::class.qualifiedName} "
                                    + "'$expected' but got ${actual::class.qualifiedName} '$actual'"),
                    boundDynamicValues = emptyMap()
                )
            }
        }
    }

    fun compareAndBindMap(
        expected: Map<*, *>,
        actual: Any,
        path: String,
        strict: Boolean
    ): TestMatchResult {
        if (actual !is Map<*, *>) {
            return TestMatchResult(
                errors = listOf("expected to find a map at path $path but found ${actual::class.simpleName}: $actual"),
                boundDynamicValues = emptyMap()
            )
        }

        val errors = mutableListOf<String>()
        val dynamicBindings = mutableMapOf<DynamicBindingKey, Any?>()
        if (strict) {
            val excessiveKeys = actual.keys.toSet() - expected.keys
            if (excessiveKeys.isNotEmpty()) {
                errors += "unexpected data is found at paths ${excessiveKeys.joinToString { "$path.$it" }}" +
                          excessiveKeys.joinToString { "$it: ${actual[it]}" }
            }
        }
        for ((key, value) in expected) {
            if (value == NOT_SET_MARKER) {
                actual[key]?.let {
                    errors += ("expected that no value is set at path $path.$key but there is a value of "
                               + "type ${it::class.simpleName}: $it")
                }
            } else {
                actual[key]?.let {
                    val result = compareAndBind(value as Any, it, "$path.$key", strict)
                    errors += result.errors
                    dynamicBindings += result.boundDynamicValues
                } ?: run {
                    errors += "mismatch at path '$path.$key' - expected to find a ${value?.javaClass?.name} " +
                              "value but got null"
                }
            }
        }
        return TestMatchResult(errors, dynamicBindings)
    }

    fun compareAndBindList(
        expected: List<*>,
        actual: Any,
        path: String,
        strict: Boolean
    ): TestMatchResult {
        if (actual !is List<*>) {
            return TestMatchResult(
                errors = listOf("expected to find a list at path $path but found ${actual::class.simpleName}: $actual"),
                boundDynamicValues = emptyMap()
            )
        }
        return if (strict) {
            compareAndBindListInStrictMode(expected, actual, path)
        } else {
            compareAndBindListInNonStrictMode(expected, actual, path)
        }
    }

    fun compareAndBindListInStrictMode(
        expected: List<*>,
        actual: List<*>,
        path: String
    ): TestMatchResult {
        return if (expected.size != actual.size) {
            TestMatchResult(
                errors = listOf(
                    "unexpected entry(-ies) found at path '$path' - expected ${expected.size} " +
                    "elements but got ${actual.size} ($expected VS $actual)"
                ),
                boundDynamicValues = emptyMap()
            )
        } else {
            val errors = mutableListOf<String>()
            val dynamicBindings = mutableMapOf<DynamicBindingKey, Any?>()
            expected.forEachIndexed { i: Int, expectedValue: Any? ->
                expectedValue ?: fail("I can't happen, path: $path, index: $i")
                actual[i]?.let {
                    val result = compareAndBind(expectedValue, it, "$path[$i]", true)
                    errors += result.errors
                    dynamicBindings += result.boundDynamicValues
                } ?: run {
                    errors += "mismatch at path '$path[$i]' - expected to find a " +
                              "${expectedValue::class.qualifiedName} '$expectedValue' but got null"
                }
            }
            TestMatchResult(errors, dynamicBindings)
        }
    }

    fun compareAndBindListInNonStrictMode(
        expected: List<*>,
        actual: List<*>,
        path: String
    ): TestMatchResult {
        val errors = mutableListOf<String>()
        val dynamicBindings = mutableMapOf<DynamicBindingKey, Any?>()
        val remainingCandidates = actual.toMutableList()
        for (expectedElement in expected) {
            expectedElement ?: fail("I can't happen")
            var matched = false
            for (candidate in remainingCandidates) {
                val result = compareAndBind(expectedElement, candidate as Any, path, false)
                if (result.errors.isEmpty()) {
                    matched = true
                    remainingCandidates.remove(candidate)
                    dynamicBindings += result.boundDynamicValues
                    break
                }
            }
            if (!matched) {
                errors += "mismatch at path '$path' - expected to find a " +
                          "${expectedElement::class.qualifiedName} '$expectedElement' but got null"
            }
        }
        return TestMatchResult(errors, dynamicBindings)
    }
}