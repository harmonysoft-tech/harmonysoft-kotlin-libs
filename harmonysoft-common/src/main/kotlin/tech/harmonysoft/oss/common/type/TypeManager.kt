package tech.harmonysoft.oss.common.type

import kotlin.reflect.KClass

/**
 * We have a number of use-cases when it's necessary to do type-specific manipulations, for example,
 * parse objects from their string representations.
 *
 * This interface defines API for such type-specific operations
 */
interface TypeManager<T : Any> {

    /**
     * Target data type covered by the current manager.
     */
    val targetType: KClass<T>

    /**
     * Normally type managers are bound to particular type ([targetType] property). However, sometimes that
     * is not enough. Consider, for example [String] - normally we use it as-is but for SQL context we want
     * to parse values like `'value'` as `value` (strip single quotes).
     *
     * So, normally all objects which implement this interface should have unique combination of [targetType]
     * and this property
     */
    val targetContext: TypeManagerContext

    /**
     * Tries to parse value of the target type from its given string representation.
     *
     * @return non-`null` in case of successful parse
     */
    fun maybeParse(rawValue: String): T?

    @Suppress("UNCHECKED_CAST")
    fun compareTo(first: T, second: T?): Int? {
        if (first is Comparable<*>) {
            return second?.let {
                (first as Comparable<Any>).compareTo(it as Comparable<Any>)
            }
        } else {
            throw IllegalArgumentException(
                "build-in comparison works only for ${Comparable::class.simpleName} types "
                + "but got ${first::class.qualifiedName}"
            )
        }
    }

    fun areEqual(first: T?, second: T?): Boolean {
        return (first == null && second == null) || first?.let {
            compareTo(it, second) == 0
        } ?: false
    }
}