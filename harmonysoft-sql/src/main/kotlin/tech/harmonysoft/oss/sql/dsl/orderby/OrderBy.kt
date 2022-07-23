package tech.harmonysoft.oss.sql.dsl.orderby

import tech.harmonysoft.oss.sql.dsl.target.SqlTarget

data class OrderBy(
    val target: SqlTarget,
    val ascending: Boolean
) {

    fun replaceColumns(columns: Map<String, String>): OrderBy {
        val newTarget = target.replaceColumns(columns)
        return if (newTarget == target) {
            this
        } else {
            copy(target = newTarget)
        }
    }

    override fun toString(): String {
        return buildString {
            append(target)
            if (ascending) {
                append(" asc")
            } else {
                append(" desc")
            }
        }
    }
}