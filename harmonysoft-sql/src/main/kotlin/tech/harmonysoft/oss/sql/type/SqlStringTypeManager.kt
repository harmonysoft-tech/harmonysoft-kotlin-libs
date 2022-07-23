package tech.harmonysoft.oss.sql.type

import tech.harmonysoft.oss.common.type.TypeManager
import javax.inject.Named

@Named
class SqlStringTypeManager : TypeManager<String> {

    override val targetType = String::class

    override val targetContext = SqlTypeManagerContext.INSTANCE

    override fun maybeParse(rawValue: String): String {
        val trimmed = rawValue.trim()
        if (!trimmed.startsWith("'") || !trimmed.endsWith("'")) {
            throw IllegalArgumentException(
                "SQL strings are expected to be wrapped into single quotes but got '$rawValue'"
            )
        }
        return rawValue.substring(1, rawValue.length - 1)
    }
}