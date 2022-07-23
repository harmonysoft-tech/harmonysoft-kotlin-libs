package tech.harmonysoft.oss.sql.dsl.filter

import tech.harmonysoft.oss.sql.dsl.constraint.Constraint
import tech.harmonysoft.oss.sql.dsl.constraint.ConstraintVisitor
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget
import tech.harmonysoft.oss.sql.dsl.target.SqlTargetVisitor
import tech.harmonysoft.oss.sql.dsl.util.DslUtil

sealed class Filter {

    abstract fun <T> visit(visitor: FilterVisitor<T>): T

    fun removeValue(column: String, value: SqlTarget): Filter? {
        return removeValue(SqlTarget.Column(column), value)
    }

    fun removeValue(target: SqlTarget, value: SqlTarget): Filter? {
        val visitor = object : AbstractFilterModificationVisitor() {
            override fun visit(filter: Leaf): Filter? {
                return if (filter.target == target) {
                    filter.constraint.visit(ConstraintVisitor.removeValue(value))?.let {
                        filter.copy(constraint = it)
                    }
                } else {
                    filter
                }
            }
        }
        return visit(visitor)
    }

    /**
     * Allows to get a filter built by removing given filter from the current filter (current filter is not changed
     * by this operation).
     *
     * **Note**: given filter is compared via identity, e.g. if current filter is like `a = 1 or (b = 2 or a = 1)`
     * and this method is called with `Filter.Leaf(SqlTarget.Column("a"), Constraint.Binary(Operator.EQUAL, "1"))`
     * then it returns the same filter, i.e. `a = 1 or (b = 2 or a = 1)` because even though given filter is equal
     * to internal `a = 1` sub-filters, it's a different object
     */
    operator fun minus(toRemove: Filter): Filter? {
        return visit(object : AbstractFilterModificationVisitor() {

            override fun visit(filter: And): Filter? {
                return if (filter === toRemove) {
                    null
                } else {
                    super.visit(filter)
                }
            }

            override fun visit(filter: Or): Filter? {
                return if (filter === toRemove) {
                    null
                } else {
                    super.visit(filter)
                }
            }

            override fun visit(filter: Not): Filter? {
                return if (filter === toRemove) {
                    null
                } else {
                    super.visit(filter)
                }
            }

            override fun visit(filter: Leaf): Filter? {
                return filter.takeIf { it !== toRemove }
            }
        })
    }

    /**
     * Allows creating a filter which is made form the current one but with replaced column names (if any).
     *
     * For example, suppose that we have a filter like `a = 1 or (a = 2 and b = 3)` initially and call this method
     * with `mapOf("a" to "x", "b" to "y")`. Resulting filter would be `x = 1 or (x = 2 and y = 3)`
     */
    fun replaceColumns(columns: Map<String, String>): Filter {
        return modify {
            val newTarget = it.target.replaceColumns(columns)
            val newConstraint = it.constraint.replaceColumns(columns)
            if (newTarget == it.target && newConstraint == it.constraint) {
                it
            } else {
                it.copy(
                    target = it.target.replaceColumns(columns),
                    constraint = newConstraint
                )
            }
        } as Filter
    }

    /**
     * @return  filter containing only given columns; `null` if resulting filter is empty
     */
    fun keepColumns(vararg columns: String): Filter? {
        return keepColumns(columns.toList())
    }

    /**
     * @return  filter containing only given columns; `null` if resulting filter is empty
     */
    fun keepColumns(columns: Collection<String>): Filter? {
        return dropColumns {
            !columns.contains(it)
        }
    }

    /**
     * @return  filter without given columns; `null` if resulting filter is empty
     */
    fun dropColumns(vararg columns: String): Filter? {
        return dropColumns(columns.toList())
    }

    /**
     * @return  filter without given columns; `null` if resulting filter is empty
     */
    fun dropColumns(columns: Collection<String>): Filter? {
        return dropColumns {
            columns.contains(it)
        }
    }

    fun dropColumns(columnNamePredicate: (String) -> Boolean): Filter? {
        val targetPredicate = object : SqlTargetVisitor<Boolean> {

            override fun visit(target: SqlTarget.AllColumns): Boolean {
                return false
            }

            override fun visit(target: SqlTarget.Column): Boolean {
                return columnNamePredicate(target.name)
            }

            override fun visit(target: SqlTarget.Function): Boolean {
                return false
            }

            override fun visit(target: SqlTarget.OperatorFunction): Boolean {
                return false
            }

            override fun visit(target: SqlTarget.LongLiteral): Boolean {
                return false
            }

            override fun visit(target: SqlTarget.StringLiteral): Boolean {
                return false
            }

            override fun visit(target: SqlTarget.DoubleLiteral): Boolean {
                return false
            }

            override fun visit(target: SqlTarget.Placeholder): Boolean {
                return false
            }

            override fun visit(target: SqlTarget.SubSelect): Boolean {
                return false
            }

            override fun visit(target: SqlTarget.DateTimeLiteral): Boolean {
                return false
            }
        }
        return modify { filter ->
            filter.takeUnless { it.target.visit(targetPredicate) }
        }
    }

    fun modify(mapper: (Leaf) -> Filter?): Filter? {
        return visit(object : AbstractFilterModificationVisitor() {
            override fun visit(filter: Leaf): Filter? {
                return mapper(filter)
            }
        })
    }

    data class And(val filters: Collection<Filter>) : Filter() {

        override fun <T> visit(visitor: FilterVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return filters.joinToString(prefix = "(", separator = " and ", postfix = ")")
        }
    }

    data class Or(val filters: Collection<Filter>) : Filter() {

        override fun <T> visit(visitor: FilterVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return filters.joinToString(prefix = "(", separator = " or ", postfix = ")")
        }
    }

    data class Not(val filter: Filter): Filter() {

        override fun <T> visit(visitor: FilterVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return "not ($filter)"
        }
    }

    data class Leaf(
        val target: SqlTarget,
        val constraint: Constraint
    ) : Filter() {

        override fun <T> visit(visitor: FilterVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return buildString {
                when (target) {
                    is SqlTarget.Column -> DslUtil.appendColumn(target, this)
                    else -> append(target)
                }
                append(" ")
                append(constraint)
            }
        }
    }
}