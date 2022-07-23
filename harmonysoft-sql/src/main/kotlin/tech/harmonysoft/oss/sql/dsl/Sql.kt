package tech.harmonysoft.oss.sql.dsl

import tech.harmonysoft.oss.sql.dsl.filter.Filter
import tech.harmonysoft.oss.sql.dsl.join.Join
import tech.harmonysoft.oss.sql.dsl.operation.Operation
import tech.harmonysoft.oss.sql.dsl.orderby.OrderBy
import tech.harmonysoft.oss.sql.dsl.table.Table
import tech.harmonysoft.oss.sql.dsl.target.SelectTarget
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget
import tech.harmonysoft.oss.sql.parser.SqlParseContext
import tech.harmonysoft.oss.sql.parser.SqlType

sealed class Sql {

    abstract val sql: String

    /**
     * Allows creating an [Sql] object made from the current one but with replace column names (if any).
     *
     * For example, suppose that we have sql like `select a, b from t where a > 1 order by a` initially
     * and call this method with `mapOf("a" to "x")`. Resulting sql would be `select x, b from t where x > 1 order by x`
     */
    abstract fun replaceColumns(columns: Map<String, String>): Sql

    data class Select(
        val columns: List<SelectTarget>,
        val table: Table,
        val joins: List<Join> = emptyList(),
        val filter: Filter? = null,
        val distinct: Boolean = false,
        val top: Int? = null,
        val groupBy: List<SqlTarget> = emptyList(),
        val having: Filter? = null,
        val orderBy: List<OrderBy> = emptyList(),
        val operations: List<Operation> = emptyList()
    ) : Sql() {

        override val sql: String by lazy {
            buildString {
                append("select ")
                if (top != null) {
                    append("top ").append(top).append(" ")
                }
                if (distinct) {
                    append("distinct ")
                }
                append(columns.joinToString())
                append(" from ")
                append(table.toString())
                joins.forEach { join ->
                    append(join.toString())
                }
                filter?.let { append(" where $it") }
                if (groupBy.isNotEmpty()) {
                    append(" group by ")
                    append(groupBy.joinToString())
                }
                having?.let { append(" having $it") }
                if (orderBy.isNotEmpty()) {
                    append(" order by ")
                    append(orderBy.joinToString())
                }
                operations.forEach {
                    append(" ${it.type} ")
                    append(it.select.sql)
                }
            }
        }

        init {
            if (having != null && groupBy.isEmpty()) {
                throw IllegalArgumentException("can't have non-empty 'having by' ($having) and empty 'group by'")
            }
        }

        override fun replaceColumns(columns: Map<String, String>): Sql {
            val newColumns = this.columns.map { it.replaceColumns(columns) }
            val newFilter = filter?.replaceColumns(columns)
            val newGroupBy = groupBy.map { it.replaceColumns(columns) }
            val newOrderBy = orderBy.map { it.replaceColumns(columns) }
            return if (newColumns == this.columns && newFilter == filter && newGroupBy == groupBy
                       && newOrderBy == orderBy
            ) {
                this
            } else {
                copy(
                    columns = newColumns,
                    filter = newFilter,
                    groupBy = newGroupBy,
                    orderBy = newOrderBy
                )
            }
        }

        companion object {

            /**
             * Sometimes we need to have a non-null `Select` reference, for example, in case of config like this:
             *
             * ```
             * data class MyConfig(
             *     val enabled: Boolean,
             *     val sql1: Sql.Select,
             *     val sql2: Sql.Select
             * )
             * ```
             *
             * If it has `enabled=false`, then the application doesn't use it. However, `sql1` and `sql2` fields
             * are non-null here, so, we need to have valid references there. This object can be used in such cases.
             */
            val NULL_OBJECT = Select(
                columns = emptyList(),
                table = Table("")
            )

            fun fromContext(context: SqlParseContext): Select {
                return when (context.type) {
                    SqlType.QUERY -> Select(
                        columns = context.targets.takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException(
                            "can't parse columns from '${context.sql}'"
                        ),
                        table = context.table ?: context.subSelect?.let { Table(it, context.subSelectAlias) }
                                ?: throw IllegalArgumentException("can't parse table(s) or sub-select " +
                                                                  "from '$context.sql'"),
                        joins = context.joins,
                        filter = context.filter,
                        distinct = context.distinct,
                        top = context.top,
                        groupBy = context.groupBy,
                        having = context.having,
                        orderBy = context.orderBy,
                        operations = context.operations
                    )
                    else -> throw IllegalArgumentException("can't parse '${context.sql}'")
                }
            }
        }
    }
}