package tech.harmonysoft.oss.common.type

import javax.inject.Named

@Named
class DefaultStringTypeManager : TypeManager<String> {

    override val targetType = String::class

    override val targetContext = TypeManagerContext.DEFAULT

    override fun maybeParse(rawValue: String): String {
        return rawValue.trim()
    }
}