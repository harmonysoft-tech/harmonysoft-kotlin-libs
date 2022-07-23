package tech.harmonysoft.oss.sql.parser

import net.sf.jsqlparser.expression.*
import net.sf.jsqlparser.expression.Function
import net.sf.jsqlparser.expression.operators.arithmetic.Addition
import net.sf.jsqlparser.expression.operators.arithmetic.Division
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction
import net.sf.jsqlparser.expression.operators.conditional.AndExpression
import net.sf.jsqlparser.expression.operators.conditional.OrExpression
import net.sf.jsqlparser.expression.operators.relational.*
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.statement.select.AllColumns
import net.sf.jsqlparser.statement.select.SubSelect
import tech.harmonysoft.oss.sql.ast.delegation.DelegatingExpressionVisitorAdapter
import tech.harmonysoft.oss.sql.dsl.Sql
import tech.harmonysoft.oss.sql.dsl.constraint.Constraint
import tech.harmonysoft.oss.sql.dsl.constraint.Operator
import tech.harmonysoft.oss.sql.dsl.filter.Filter
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget

class ParsingExpressionVisitor(
    private val context: SqlParseContext
) : DelegatingExpressionVisitorAdapter() {

    override fun handleExpression(expression: Expression) {
        throw IllegalArgumentException(
            "unsupported expression of class ${expression::class.qualifiedName}: $expression"
        )
    }

    override fun visit(addition: Addition) {
        context.onTarget(SqlTarget.OperatorFunction(
            operator = "+",
            left = context.buildTarget(addition.leftExpression),
            right = context.buildTarget(addition.rightExpression)
        ))
    }

    override fun visit(subtraction: Subtraction) {
        context.onTarget(SqlTarget.OperatorFunction(
            operator = "-",
            left = context.buildTarget(subtraction.leftExpression),
            right = context.buildTarget(subtraction.rightExpression)
        ))
    }

    override fun visit(longValue: LongValue) {
        context.onTarget(SqlTarget.LongLiteral(longValue.value))
    }

    override fun visit(doubleValue: DoubleValue) {
        context.onTarget(SqlTarget.DoubleLiteral(doubleValue.value))
    }

    override fun visit(parenthesis: Parenthesis) {
        parenthesis.expression.accept(this)
    }

    override fun visit(inExpression: InExpression) {
        val target = context.buildTarget(inExpression.leftExpression)
        val values = when (val rightItemsList = inExpression.rightItemsList) {
            is ExpressionList -> rightItemsList.expressions.map {
                context.buildTarget(it)
            }

            is SubSelect -> {
                val subContext = SqlParseContext(rightItemsList.selectBody.toString())
                subContext.type = SqlType.QUERY
                rightItemsList.selectBody.accept(subContext.visitor.select)
                listOf(SqlTarget.SubSelect(Sql.Select.fromContext(subContext), false))
            }

            else -> throw IllegalArgumentException(
                "can't parse target IN expression values from '$inExpression'. Expected them to be store as "
                + "${ExpressionList::class.qualifiedName} or ${SubSelect::class.qualifiedName} but got "
                + "${inExpression.rightItemsList::class.qualifiedName} instead"
            )
        }

        val constraint = if (inExpression.isNot) {
            Constraint.NotIn(values)
        } else {
            Constraint.In(values)
        }
        context.onFilter(Filter.Leaf(target, constraint))
    }

    override fun visit(isNullExpression: IsNullExpression) {
        val target = context.buildTarget(isNullExpression.leftExpression)
        val constraint = if (isNullExpression.isNot) {
            Constraint.IsNotNull
        } else {
            Constraint.IsNull
        }
        context.onFilter(Filter.Leaf(target, constraint))
    }

    override fun visit(likeExpression: LikeExpression) {
        val operator = if (likeExpression.isNot) {
            Operator.NOT_LIKE
        } else {
            Operator.LIKE
        }
        processBinaryExpression(likeExpression, operator)
    }

    override fun visit(equalsTo: EqualsTo) {
        processBinaryExpression(equalsTo, Operator.EQUAL)
    }

    override fun visit(notEqualsTo: NotEqualsTo) {
        processBinaryExpression(notEqualsTo, Operator.NOT_EQUAL)
    }

    override fun visit(greaterThan: GreaterThan) {
        processBinaryExpression(greaterThan, Operator.GREATER)
    }

    override fun visit(greaterThanEquals: GreaterThanEquals) {
        processBinaryExpression(greaterThanEquals, Operator.GREATER_OR_EQUAL)
    }

    override fun visit(minorThan: MinorThan) {
        processBinaryExpression(minorThan, Operator.LESS)
    }

    override fun visit(minorThanEquals: MinorThanEquals) {
        processBinaryExpression(minorThanEquals, Operator.LESS_OR_EQUAL)
    }

    private fun processBinaryExpression(expression: BinaryExpression, operator: Operator) {
        val leftTarget = context.buildTarget(expression.leftExpression)
        val target = context.buildTarget(expression.rightExpression)
        context.onFilter(Filter.Leaf(
            target = leftTarget,
            constraint = Constraint.Binary(operator, target)
        ))
    }

    override fun visit(between: Between) {
        val target = context.buildTarget(between.leftExpression)
        val constraint = Constraint.Between(
            context.buildTarget(between.betweenExpressionStart),
            context.buildTarget(between.betweenExpressionEnd)
        )
        context.onFilter(Filter.Leaf(target, constraint))
    }

    override fun visit(andExpression: AndExpression) {
        buildCompositeFilter(andExpression)
    }

    override fun visit(orExpression: OrExpression) {
        buildCompositeFilter(orExpression)
    }

    private fun flatten(expression: BinaryExpression): List<Expression> {
        // AST for input like 'e1 and e2 and e3' looks as below:
        //                and
        //               /   \
        //            and     e3
        //           /   \
        //         e1    e2
        // Here we take the root and want to return a list of expressions where all expressions of the same type
        // ad root are flattened, e.g. for example above that would be just [e1, e2, e3]
        val result = mutableListOf<Expression>()
        flatten(expression, result)
        return result
    }

    private fun flatten(expression: BinaryExpression, holder: MutableList<Expression>) {
        if (expression.leftExpression::class == expression::class) {
            flatten(expression.leftExpression as BinaryExpression, holder)
        } else {
            holder += expression.leftExpression
        }
        if (expression.rightExpression::class == expression::class) {
            flatten(expression.rightExpression as BinaryExpression, holder)
        } else {
            holder += expression.rightExpression
        }
    }

    private fun buildCompositeFilter(expression: BinaryExpression) {
        context.buildCompositeFilter(expression is AndExpression) {
            val flattened = flatten(expression)
            for (e in flattened) {
                e.accept(this)
            }
        }
    }

    override fun visit(division: Division) {
        context.onTarget(SqlTarget.OperatorFunction(
            operator = "/",
            left = context.buildTarget(division.leftExpression),
            right = context.buildTarget(division.rightExpression)
        ))
    }

    override fun visit(multiplication: Multiplication) {
        context.onTarget(SqlTarget.OperatorFunction(
            operator = "*",
            left = context.buildTarget(multiplication.leftExpression),
            right = context.buildTarget(multiplication.rightExpression)
        ))
    }

    override fun visit(tableColumn: Column) {
        context.onTarget(context.buildTarget(tableColumn))
    }

    override fun visit(function: Function) {
        context.onTarget(context.buildTarget(function))
    }

    override fun visit(aThis: NotExpression) {
        context.buildNot {
            aThis.expression.accept(this)
        }
    }

    override fun visit(stringValue: StringValue) {
        context.onTarget(SqlTarget.StringLiteral(stringValue.value))
    }

    override fun visit(allColumns: AllColumns) {
        context.onTarget(SqlTarget.AllColumns)
    }
}