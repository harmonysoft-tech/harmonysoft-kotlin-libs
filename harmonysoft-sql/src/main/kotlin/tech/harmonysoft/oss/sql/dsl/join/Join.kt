package tech.harmonysoft.oss.sql.dsl.join

import tech.harmonysoft.oss.sql.dsl.filter.Filter
import tech.harmonysoft.oss.sql.dsl.table.Table

data class Join(
    val table: Table,
    val on: Filter?,
    val method: JoinMethod
) {

    override fun toString(): String {
        return buildString {
            if (method == JoinMethod.SIMPLE) {
                append(", ")
            } else {
                append(" ")
                append(method.name.lowercase())
                append(" join ")
            }
            append(table.toString())
            on?.let {
                append(" on $it")
            }
        }
    }
}