package tech.harmonysoft.oss.common.type

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateTypeManager(
    override val targetContext: TypeManagerContext,
    _dateFormat: String
) : TypeManager<LocalDate> {

    private val formatter: DateTimeFormatter

    override val targetType = LocalDate::class

    init {
        try {
            formatter = DateTimeFormatter.ofPattern(_dateFormat)
        } catch (e: Exception) {
            throw IllegalArgumentException("bad date format '$_dateFormat'", e)
        }
    }

    override fun maybeParse(rawValue: String): LocalDate? {
        return rawValue.trim().takeIf(String::isNotEmpty)?.let {
            LocalDate.parse(it, formatter)
        }
    }
}