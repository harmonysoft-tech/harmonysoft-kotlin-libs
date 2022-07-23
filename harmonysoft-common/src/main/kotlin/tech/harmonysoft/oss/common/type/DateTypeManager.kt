package tech.harmonysoft.oss.common.type

import java.text.SimpleDateFormat
import java.util.*

class DateTypeManager(
    override val targetContext: TypeManagerContext,
    _dateFormat: String
) : TypeManager<Date> {

    private val formatter = ThreadLocal.withInitial { SimpleDateFormat(_dateFormat) }

    override val targetType = Date::class

    init {
        try {
            formatter.get()
        } catch (e: Exception) {
            throw IllegalArgumentException("bad date format '$_dateFormat'", e)
        }
    }

    override fun maybeParse(rawValue: String): Date? {
        return rawValue.trim().takeIf(String::isNotEmpty)?.let {
            formatter.get().parse(it)
        }
    }
}