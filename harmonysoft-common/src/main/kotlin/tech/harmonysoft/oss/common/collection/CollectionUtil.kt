package tech.harmonysoft.oss.common.collection

import tech.harmonysoft.oss.common.string.util.isNotBlankEffective

inline fun <T, R> Iterable<T>.mapFirstNotNull(transform: (T) -> R?): R? {
    for (item in this) {
        val result = transform(item)
        if (result != null) {
            return result
        }
    }
    return null
}

object CollectionUtil {

    /**
     * Converts nested maps and collections in the given parameter to composite string keys.
     *
     * Example:
     *
     * ```
     * input:
     * mapOf(
     *   "data" to mapOf(
     *     "key1" to "value1",
     *     "key2" to listOf("value2", "value3")
     *   )
     * )
     * ```
     *
     * ```
     * output:
     * mapOf(
     *   "data.key1" to "value1",
     *   "data.key2[0]" to "value2",
     *   "data.key2[1]" to "value3",
     * )
     * ```
     */
    fun flatten(data: Map<String, Any?>): Map<String, Any?> {
        return (data.flatMap { (key, value) ->
            value?.let {
                flatten(key, it)
            } ?: emptyList()
        }).toMap()
    }

    private fun flatten(path: String, value: Any): Collection<Pair<String, Any>> {
        return when (value) {
            is Map<*, *> -> value.entries.flatMap { (k, v) ->
                v?.let {
                    flatten("$path.$k", v)
                } ?: emptyList()
            }

            is List<*> -> value.flatMapIndexed { index, v ->
                v?.let {
                    flatten("$path[$index]", it)
                } ?: emptyList()
            }

            else -> listOf(path to value)
        }
    }

    /**
     * Reverse operation to [flatten], example:
     *
     * ```
     * input:
     * mapOf(
     *   "data.key1" to "value1",
     *   "data.key2[0]" to "value2",
     *   "data.key2[1]" to "value3",
     * )
     * ```
     *
     * ```
     * output:
     * mapOf(
     *   "data" to mapOf(
     *     "key1" to "value1",
     *     "key2" to listOf("value2", "value3")
     *   )
     * )
     * ```
     */
    @Suppress("UNCHECKED_CAST")
    fun unflatten(input: Map<String, *>): Map<String, Any> {
        var result: Data? = null
        for ((key, value) in input) {
            value?.let {
                val data = unflatten(key, Data.LeafData(it))
                result?.let {
                    merge(data, it)
                } ?: run {
                    result = data
                }
            }
        }
        return result?.let {
            replaceDataHolders(it) as Map<String, Any>
        } ?: emptyMap()
    }

    private fun merge(from: Data, to: Data) {
        when (from) {
            is Data.MapData -> if (to is Data.MapData) {
                merge(from, to)
            } else {
                throw IllegalArgumentException(
                    "wrong data setup - there are values of type ${from::class.simpleName} and " +
                    "${to::class.simpleName}"
                )
            }

            is Data.ArrayData -> if (to is Data.ArrayData) {
                merge(from, to)
            } else {
                throw IllegalArgumentException(
                    "wrong data setup - there are values of type ${from::class.simpleName} and " +
                    "${to::class.simpleName}"
                )
            }

            is Data.LeafData -> throw IllegalArgumentException(
                "wrong data setup - duplicate leaf value '${from.value}' vs $to"
            )
        }
    }

    private fun merge(from: Data.MapData, to: Data.MapData) {
        for ((key, value) in from.data) {
            val existingValue = to.data[key]
            if (existingValue == null) {
                to.data[key] = value
            } else {
                merge(value, existingValue)
            }
        }
    }

    private fun merge(from: Data.ArrayData, to: Data.ArrayData) {
        for ((index, value) in from.byIndex) {
            val existingValue = to.byIndex[index]
            if (existingValue == null) {
                to.byIndex[index] = value
            } else {
                merge(value, existingValue)
            }
        }
    }

    private fun replaceDataHolders(from: Data): Any {
        return when (from) {
            is Data.MapData -> from.data.mapValues { (_, v) -> replaceDataHolders(v) }
            is Data.ArrayData -> from.byIndex.entries.sortedBy { it.key }.map { replaceDataHolders(it.value) }
            is Data.LeafData -> from.value
        }
    }

    private fun unflatten(key: String, value: Data): Data {
        var currentKey = key
        var currentValue: Data = value
        while (currentKey.isNotBlankEffective()) {
            val dotIndex = currentKey.lastIndexOf('.')
            val bracketIndex = currentKey.lastIndexOf(']')
            when {
                dotIndex < 0 && bracketIndex < 0 -> {
                    currentValue = Data.MapData(currentKey, currentValue)
                    break
                }

                dotIndex >= 0 && bracketIndex < dotIndex -> {
                    val subKey = currentKey.substring(dotIndex + 1)
                    currentValue = Data.MapData(subKey, currentValue)
                    currentKey = currentKey.substring(0, dotIndex)
                }

                else -> {
                    val openingBracketIndex = currentKey.lastIndexOf('[')
                    val index = currentKey.substring(openingBracketIndex + 1, bracketIndex).toInt()
                    currentKey = currentKey.substring(0, openingBracketIndex)
                    currentValue = Data.ArrayData(index, currentValue)
                }
            }
        }
        return currentValue
    }

    private sealed interface Data {

        class ArrayData(
            index: Int,
            data: Data
        ) : Data {

            val byIndex = mutableMapOf<Int, Data>().apply {
                this[index] = data
            }

            override fun toString(): String {
                return byIndex.entries.joinToString(prefix = "[", postfix = "]") { it.value.toString() }
            }
        }

        class MapData(
            key: String,
            value: Data
        ) : Data {

            val data = mutableMapOf<String, Data>().apply {
                this[key] = value
            }

            override fun toString(): String {
                return data.toString()
            }
        }

        class LeafData(
            val value: Any
        ) : Data {

            override fun toString(): String {
                return value.toString()
            }
        }
    }



    // k1.k2[0][1].k3 = 1
    // mapOf(
    //   "k1" to listOf(
    //     listOf(
    //       mapOf("k3" to 1)
    //     )
    //   )
    // )
}