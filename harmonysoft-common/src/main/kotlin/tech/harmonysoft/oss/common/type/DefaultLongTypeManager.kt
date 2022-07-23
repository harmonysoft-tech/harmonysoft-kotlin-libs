package tech.harmonysoft.oss.common.type

import javax.inject.Named

@Named
class DefaultLongTypeManager : TypeManager<Long> {

    override val targetType = Long::class

    override val targetContext = TypeManagerContext.DEFAULT

    override fun maybeParse(rawValue: String): Long? {
        return rawValue.trim().takeIf(String::isNotEmpty)?.toLong()
    }
}