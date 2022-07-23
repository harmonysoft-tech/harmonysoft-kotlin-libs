package tech.harmonysoft.oss.common.type

import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeTypeManager(
    override val targetContext: TypeManagerContext,
    _timeFormat: String
) : TypeManager<LocalTime> {

    private val formatter: DateTimeFormatter

    override val targetType = LocalTime::class

    init {
        try {
            formatter = DateTimeFormatter.ofPattern(_timeFormat)
        } catch (e: Exception) {
            throw IllegalArgumentException("bad time format '$_timeFormat'", e)
        }
    }

    override fun maybeParse(rawValue: String): LocalTime? {
        return rawValue.trim().takeIf(String::isNotEmpty)?.let {
            LocalTime.parse(it, formatter)
        }
    }
}