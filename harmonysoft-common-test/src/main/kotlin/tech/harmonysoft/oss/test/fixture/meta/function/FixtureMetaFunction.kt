package tech.harmonysoft.oss.test.fixture.meta.function

import tech.harmonysoft.oss.test.fixture.meta.value.FixtureMetaValueMapper

/**
 * [FixtureMetaValueMapper] interface handles simple meta values like `<free-port>` when there are no arguments
 * to be processed. But sometimes we need more complex processing. For example, we might want to convert provided
 * date-time string to UTC and specify it in test like `toUtc(2022-03-13 12:00:00)`. Here `toUtc` is a prefix
 * or function name and date is an argument or input value.
 *
 * This interface defines contract for a strategy which allows to apply an action specified by the prefix
 * to the argument
 */
interface FixtureMetaFunction {

    /**
     * Function name to match with meta value prefix.
     *
     * For example, when meta value is defined as `toUtc(2022-03-13 12:00:00)`, backing class which implements
     * it should return `toUtc` from this property
     */
    val functionName: String

    /**
     * Applies function logic to the [value]. It's expected that the [value] is provided without prefix.
     * For example, if this meta function handles cases like `toUtc(2022-03-13 12:00:00)`, it would receive
     * `2022-03-13 12:00:00` as a parameter in this method
     */
    fun applyFunction(value: String): String?
}