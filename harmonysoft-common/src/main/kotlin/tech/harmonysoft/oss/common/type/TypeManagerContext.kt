package tech.harmonysoft.oss.common.type

import java.time.LocalDateTime

/**
 * Normally [type managers][TypeManager] are bound to particular type ([TypeManager.targetType] property).
 * However, sometimes that is not enough. Consider, for example, [LocalDateTime] - when we
 * [parse][TypeManager.maybeParse] it from string, we need to use particular format. Different use-cases
 * might require different formats.
 *
 * Another example is [String] - normally we use it as-is but for SQL context we want to parse values like
 * `'value'` as `value` (strip single quotes).
 *
 * So, normally al [TypeManager] implementations should have unique combination of [TypeManager.targetType]
 * and [TypeManager.targetContext]
 */
data class TypeManagerContext(val id: String) {

    override fun toString(): String {
        return id
    }

    companion object {

        val DEFAULT = TypeManagerContext("default")
    }
}