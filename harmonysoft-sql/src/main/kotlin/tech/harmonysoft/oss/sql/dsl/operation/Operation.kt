package tech.harmonysoft.oss.sql.dsl.operation

import tech.harmonysoft.oss.sql.dsl.Sql

data class Operation(
    val type: String,
    val select: Sql.Select
) {

    override fun toString(): String {
        return " $type $select"
    }
}