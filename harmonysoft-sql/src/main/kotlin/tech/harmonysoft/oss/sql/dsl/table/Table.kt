package tech.harmonysoft.oss.sql.dsl.table

import tech.harmonysoft.oss.sql.dsl.Sql

data class Table(
    val name: String?,
    val select: Sql.Select?,
    val alias: String?,
    val schemaName: String?
) {

    constructor(name: String, alias: String? = null, schemaName: String? = null) : this(
        name = name,
        select = null,
        alias = alias,
        schemaName = schemaName
    )

    constructor(select: Sql.Select?, alias: String?) : this(
        name = null,
        select = select,
        alias = alias,
        schemaName = null
    )

    init {
        if (name != null && select != null) {
            throw IllegalArgumentException("table should have only name or select")
        }
    }

    override fun toString(): String {
        return buildString {
            name?.let {
                schemaName?.let {
                    append("$schemaName.")
                }
                append(name)
            }
            select?.let {
                append("(${select.sql})")
            }
            alias?.let {
                append(" $alias")
            }
        }
    }
}