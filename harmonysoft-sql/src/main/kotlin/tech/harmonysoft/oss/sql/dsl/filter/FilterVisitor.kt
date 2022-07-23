package tech.harmonysoft.oss.sql.dsl.filter

interface FilterVisitor<T> {

    fun visit(filter: Filter.And): T

    fun visit(filter: Filter.Or): T

    fun visit(filter: Filter.Not): T

    fun visit(filter: Filter.Leaf): T
}