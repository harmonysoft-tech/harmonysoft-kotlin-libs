package tech.harmonysoft.oss.common.collection

object MapUtil {

    /**
     * Converts nested maps in the given parameter to composite string keys.
     *
     * Example:
     *
     * ```
     * input:
     * mapOf(
     *   "data" to mapOf(
     *     "key" to "value"
     *   )
     * )
     * ```
     *
     * ```
     * output:
     * mapOf(
     *   "data.key" to "value"
     * )
     * ```
     */
    @Suppress("UNCHECKED_CAST")
    fun flatten(data: Map<String, Any?>): Map<String, Any?> {
        return data.flatMap { (key, value) ->
            if (value is Map<*, *>) {
                flatten(value as Map<String, Any>).map { (k, v) ->
                    "$key.$k" to v
                }
            } else {
                listOf(key to value)
            }
        }.toMap()
    }
}