package tech.harmonysoft.oss.sql.dsl.util

import tech.harmonysoft.oss.sql.dsl.target.SqlTarget

object DslUtil {

    internal fun appendColumn(column: SqlTarget.Column, buffer: StringBuilder) {
        column.table?.let {
            buffer.append(it).append(".")
        }
        buffer.append(column.name)
    }
}