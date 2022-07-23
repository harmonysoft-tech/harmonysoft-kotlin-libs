package tech.harmonysoft.oss.sql.dsl.target

import net.sf.jsqlparser.expression.DateTimeLiteralExpression
import tech.harmonysoft.oss.sql.dsl.Sql

sealed class SqlTarget {

    abstract fun <T> visit(visitor: SqlTargetVisitor<T>): T

    fun replaceColumns(columns: Map<String, String>): SqlTarget {
        return visit(object: SqlTargetVisitor<SqlTarget> {
            override fun visit(target: AllColumns): SqlTarget {
                return target
            }

            override fun visit(target: Column): SqlTarget {
                return columns[target.name]?.let { newName ->
                    target.copy(name = newName)
                } ?: target
            }

            override fun visit(target: Function): SqlTarget {
                val newOperands = target.operands.map { it.replaceColumns(columns) }
                return if (newOperands == target.operands) {
                    target
                } else {
                    target.copy(operands = newOperands)
                }
            }

            override fun visit(target: OperatorFunction): SqlTarget {
                val newLeft = target.left.replaceColumns(columns)
                val newRight = target.right.replaceColumns(columns)
                return if (newLeft == target.left && newRight == target.right) {
                    target
                } else {
                    target.copy(
                        left = newLeft,
                        right = newRight
                    )
                }
            }

            override fun visit(target: LongLiteral): SqlTarget {
                return target
            }

            override fun visit(target: StringLiteral): SqlTarget {
                return target
            }

            override fun visit(target: DoubleLiteral): SqlTarget {
                return target
            }

            override fun visit(target: Placeholder): SqlTarget {
                return target
            }

            override fun visit(target: SubSelect): SqlTarget {
                return target
            }

            override fun visit(target: DateTimeLiteral): SqlTarget {
                return target
            }
        })
    }

    object AllColumns : SqlTarget() {

        override fun <T> visit(visitor: SqlTargetVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return "*"
        }
    }

    data class SubSelect(
        val select: Sql.Select,
        val withParentheses: Boolean
    ) : SqlTarget() {

        override fun <T> visit(visitor: SqlTargetVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return if (withParentheses) {
                "(${select.sql})"
            } else {
                select.sql
            }
        }
    }

    data class Column(
        val name: String,
        val table: String?
    ) : SqlTarget() {

        constructor(name: String) : this(name = name, table = null)

        override fun <T> visit(visitor: SqlTargetVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return buildString {
                table?.let {
                    append(it).append(".")
                }
                append(name)
            }
        }
    }

    data class Function(
        val name: String,
        val operands: List<SqlTarget>
    ) : SqlTarget() {

        override fun <T> visit(visitor: SqlTargetVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return "$name(${operands.joinToString()})"
        }
    }

    data class OperatorFunction(
        val operator: String,
        val left: SqlTarget,
        val right: SqlTarget,
        val withParentheses: Boolean = false
    ) : SqlTarget() {

        override fun <T> visit(visitor: SqlTargetVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return if (withParentheses) {
                "($left $operator $right)"
            } else {
                "$left $operator $right"
            }
        }
    }

    data class LongLiteral(val value: Long) : SqlTarget() {

        override fun <T> visit(visitor: SqlTargetVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return value.toString()
        }
    }

    data class DoubleLiteral(val value: Double) : SqlTarget() {

        override fun <T> visit(visitor: SqlTargetVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return value.toString()
        }
    }

    data class StringLiteral(val value: String) : SqlTarget() {

        override fun <T> visit(visitor: SqlTargetVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return "'$value'"
        }
    }

    data class DateTimeLiteral(
        val value: String,
        val type: DateTimeLiteralExpression.DateTime
    ) : SqlTarget() {

        override fun <T> visit(visitor: SqlTargetVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return "$type $value"
        }
    }

    /**
     * Sometimes we need very flexible DSL container to hold generic value. For example, JDBC prepared statements
     * use syntax like `update table set column = ? where <condition>`. Here `?` is a placeholder which doesn't
     * exist in SQL, but is used in specific context (JDBC). We need to handle it if we implement various
     * JDBC operations on top of SQL DSL.
     *
     * This class stands for such placeholder
     */
    data class Placeholder(val value: String) : SqlTarget() {

        override fun <T> visit(visitor: SqlTargetVisitor<T>): T {
            return visitor.visit(this)
        }

        override fun toString(): String {
            return value
        }
    }
}