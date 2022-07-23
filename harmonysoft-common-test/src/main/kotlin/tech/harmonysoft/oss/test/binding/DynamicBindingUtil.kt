package tech.harmonysoft.oss.test.binding

object DynamicBindingUtil {

    /**
     * Common regex for capturing dynamic value to bind.
     */
    val TO_BIND_REGEX = """<bind:(.+)>""".toRegex()

    val DYNAMIC_BOUND_VALUE_REGEX = """bound:(.+)""".toRegex()
}