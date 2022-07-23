package tech.harmonysoft.oss.common.type

import javax.inject.Named

@Named
class DefaultIntTypeManager : TypeManager<Int> {

    override val targetType = Int::class

    override val targetContext = TypeManagerContext.DEFAULT

    override fun maybeParse(rawValue: String): Int? {
        return rawValue.trim().takeIf(String::isNotEmpty)?.toInt()
    }
}