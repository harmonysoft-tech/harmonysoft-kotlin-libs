package tech.harmonysoft.oss.sql.dsl.constraint

import tech.harmonysoft.oss.sql.dsl.target.SqlTarget
import tech.harmonysoft.oss.sql.dsl.util.DslUtil

sealed class Constraint {

    abstract fun <T> visit(visitor: ConstraintVisitor<T>): T

    /**
     * Allows creating a constraint which is made from the current one but with replaced column names (if any).
     *
     * For example, suppose that we have a constraint like `!= a` initially and call this method with
     * `mapOf("a" to "x")`. Resulting constraint would be `!= x`
     */
    fun replaceColumns(columns: Map<String, String>): Constraint {
        return visit(object : ConstraintVisitor<Constraint> {

            override fun visit(constraint: Binary): Constraint {
                val newTarget = constraint.target.replaceColumns(columns)
                return if (newTarget == constraint.target) {
                    constraint
                } else {
                    constraint.copy(target = newTarget)
                }
            }

            override fun visit(constraint: In): Constraint {
                return constraint
            }

            override fun visit(constraint: NotIn): Constraint {
                return constraint
            }

            override fun visit(constraint: IsNull): Constraint {
                return constraint
            }

            override fun visit(constraint: IsNotNull): Constraint {
                return constraint
            }

            override fun visit(constraint: Between): Constraint {
                return constraint
            }
        })
    }

    data class Binary(
        val operator: Operator,
        val target: SqlTarget
    ) : Constraint() {

        override fun <T> visit(visitor: ConstraintVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return buildString {
                when (operator) {
                    Operator.EQUAL -> append("= ")
                    Operator.NOT_EQUAL -> append("!= ")
                    Operator.LIKE -> append("like ")
                    Operator.NOT_LIKE -> append("not like ")
                    Operator.GREATER -> append("> ")
                    Operator.GREATER_OR_EQUAL -> append(">= ")
                    Operator.LESS -> append("< ")
                    Operator.LESS_OR_EQUAL -> append("<= ")
                }
                if (target is SqlTarget.Column) {
                    DslUtil.appendColumn(target, this)
                } else {
                    append(target)
                }
            }
        }
    }

    object IsNull : Constraint() {

        override fun <T> visit(visitor: ConstraintVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return "is null"
        }
    }

    object IsNotNull : Constraint() {

        override fun <T> visit(visitor: ConstraintVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return "is not null"
        }
    }

    data class In(val targetValues: Collection<SqlTarget>) : Constraint() {

        override fun <T> visit(visitor: ConstraintVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return "in (${targetValues.joinToString()})"
        }
    }

    data class NotIn(val targetValues: Collection<SqlTarget>) : Constraint() {

        override fun <T> visit(visitor: ConstraintVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return "not in (${targetValues.joinToString()})"
        }
    }

    data class Between(
        val min: SqlTarget,
        val max: SqlTarget
    ) : Constraint() {

        override fun <T> visit(visitor: ConstraintVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return "between $min and $max"
        }
    }
}