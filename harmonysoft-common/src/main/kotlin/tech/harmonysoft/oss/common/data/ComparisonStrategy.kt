package tech.harmonysoft.oss.common.data

import kotlin.reflect.KClass

interface ComparisonStrategy {

    /**
     * Like [Comparator] but night return `null` as an indication that comparison can't be performed,
     * e.g. when we try to compare `null` and not-`null`.
     *
     * Note: exact logic is specific for the type and business use-case, for example, sometimes we might
     * want `compare(null, "")` to return `0`
     */
    fun <T : Any> compare(targetType: KClass<T>, first: T?, second: T?): Int?

    companion object {

        fun inverse(i: Int?): Int? {
            return when {
                i == null -> null
                i < 0 -> 1
                i > 0 -> -1
                else -> 0
            }
        }
    }
}