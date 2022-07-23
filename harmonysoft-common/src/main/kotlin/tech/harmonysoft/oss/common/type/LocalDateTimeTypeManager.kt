package tech.harmonysoft.oss.common.type

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeTypeManager(
    override val targetContext: TypeManagerContext,
    _dateTimeFormat: String
) : TypeManager<LocalDateTime> {

    private val formatter: DateTimeFormatter

    override val targetType = LocalDateTime::class

    init {
        try {
            formatter = DateTimeFormatter.ofPattern(_dateTimeFormat)
        } catch (e: Exception) {
            throw IllegalArgumentException("bad date time format '$_dateTimeFormat'", e)
        }
    }

    override fun maybeParse(rawValue: String): LocalDateTime? {
        return rawValue.trim().takeIf(String::isNotEmpty)?.let {
            LocalDateTime.parse(it, formatter)
        }
    }
}