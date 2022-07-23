package tech.harmonysoft.oss.common.type

import java.sql.Timestamp
import java.text.SimpleDateFormat

class TimestampTypeManager(
    override val targetContext: TypeManagerContext,
    _dateTimeFormat: String
) : TypeManager<Timestamp> {

    private val formatter = ThreadLocal.withInitial { SimpleDateFormat(_dateTimeFormat) }

    override val targetType = Timestamp::class

    init {
        try {
            formatter.get()
        } catch (e: Exception) {
            throw IllegalArgumentException("bad timestamp date/time format '$_dateTimeFormat'", e)
        }
    }

    override fun maybeParse(rawValue: String): Timestamp? {
        return rawValue.trim().takeIf(String::isNotEmpty)?.let {
            Timestamp(formatter.get().parse(it).time)
        }
    }
}