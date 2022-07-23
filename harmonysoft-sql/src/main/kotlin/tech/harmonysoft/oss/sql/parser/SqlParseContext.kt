package tech.harmonysoft.oss.sql.parser

import net.sf.jsqlparser.expression.*
import net.sf.jsqlparser.expression.operators.arithmetic.Addition
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.statement.select.SubSelect
import tech.harmonysoft.oss.sql.dsl.Sql
import tech.harmonysoft.oss.sql.dsl.filter.Filter
import tech.harmonysoft.oss.sql.dsl.join.Join
import tech.harmonysoft.oss.sql.dsl.join.JoinMethod
import tech.harmonysoft.oss.sql.dsl.operation.Operation
import tech.harmonysoft.oss.sql.dsl.orderby.OrderBy
import tech.harmonysoft.oss.sql.dsl.table.Table
import tech.harmonysoft.oss.sql.dsl.target.SelectTarget
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget
import java.util.*

class SqlParseContext(val sql: String) {

    private val ongoingCompositeFilters = Stack<OngoingFilter>()
    private val ongoingFunctionOperands = Stack<MutableList<SqlTarget>>()
    private var parsingHaving = false
    private var parsingJoin = false

    val visitor = Visitors(this)

    var type: SqlType? = null
    val targets = mutableListOf<SelectTarget>()
    var table: Table? = null
    var filter: Filter? = null
    val joins = mutableListOf<Join>()
    var distinct = false
    var top: Int? = null
    var groupBy = mutableListOf<SqlTarget>()
    var having: Filter? = null
    var orderBy = mutableListOf<OrderBy>()
    val operations = mutableListOf<Operation>()
    var subSelect: Sql.Select? = null
    var subSelectAlias: String? = null
    private var joinOn: Filter? = null
    private var joinTable: Table? = null

    fun buildCompositeFilter(and: Boolean, builder: () -> Unit) {
        ongoingCompositeFilters.push(OngoingFilter(and))
        builder()
        val ongoing = ongoingCompositeFilters.pop()
        if (ongoing.filters.isEmpty()) {
            throw IllegalStateException(
                "detected an empty '${if (ongoing.and) "and" else "or"}' condition"
            )
        }
        val filter = if (ongoing.and) {
            Filter.And(ongoing.filters)
        } else {
            Filter.Or(ongoing.filters)
        }
        if (ongoingCompositeFilters.isEmpty()) {
            when {
                parsingHaving -> having = filter
                parsingJoin -> joinOn = filter
                else -> this.filter = filter
            }
        } else {
            ongoingCompositeFilters.peek().filters += filter
        }
    }

    fun buildNot(builder: () -> Unit) {
        builder()
        if (ongoingCompositeFilters.isEmpty()) {
            filter?.let {
                filter = Filter.Not(it)
            } ?: throw IllegalStateException("can't build a 'not' filter from an empty filter")
        } else {
            val ongoing = ongoingCompositeFilters.peek()
            if (ongoing.filters.isEmpty()) {
                throw IllegalStateException("can't build a 'not' filter form an empty filter")
            }
            val toNegate = ongoing.filters.removeLast()
            ongoing.filters += Filter.Not(toNegate)
        }
    }

    fun onFilter(filter: Filter) {
        if (ongoingCompositeFilters.isEmpty()) {
            when {
                parsingHaving -> having = filter
                parsingJoin -> joinOn = filter
                else -> this.filter = filter
            }
        } else {
            ongoingCompositeFilters.peek().filters += filter
        }
    }

    fun parseHaving(action: () -> Unit) {
        parsingHaving = true
        try {
            action()
        } finally {
            parsingHaving = false
        }
    }

    fun parseJoin(join: net.sf.jsqlparser.statement.select.Join, action: () -> Unit) {
        parsingJoin = true
        try {
            action()
            val joinMethod = when {
                join.isCross -> JoinMethod.CROSS
                join.isOuter -> JoinMethod.OUTER
                join.isInner -> JoinMethod.INNER
                join.isLeft -> JoinMethod.LEFT
                join.isRight -> JoinMethod.RIGHT
                join.isFull -> JoinMethod.FULL
                else -> JoinMethod.SIMPLE
            }
            joinTable?.let {
                joins.add(Join(it, joinOn, joinMethod))
            }
            subSelect?.let {
                joins.add(Join(Table(it, subSelectAlias), joinOn, joinMethod))
            }
        } finally {
            parsingJoin = false
            joinTable = null
            subSelect = null
        }
    }

    private fun buildFunctionOperands(builder: () -> Unit): List<SqlTarget> {
        ongoingFunctionOperands.push(mutableListOf())
        builder()
        val operands = ongoingFunctionOperands.pop()
        if (operands.isEmpty()) {
            throw IllegalStateException("can't build function operands")
        }
        return operands
    }

    fun onTarget(target: SqlTarget) {
        ongoingFunctionOperands.peek() += target
    }

    fun buildTarget(expression: Expression, withParentheses: Boolean = false): SqlTarget {
        return when (expression) {
            is Column -> SqlTarget.Column(
                name = expression.columnName,
                table = expression.table?.name
            )
            is net.sf.jsqlparser.expression.Function -> SqlTarget.Function(
                name = expression.name,
                operands = extractFunctionOperands(expression)
            )
            is Addition -> SqlTarget.OperatorFunction(
                operator = "+",
                left = buildTarget(expression.leftExpression),
                right = buildTarget(expression.rightExpression),
                withParentheses = withParentheses
            )
            is Subtraction -> SqlTarget.OperatorFunction(
                operator = "-",
                left = buildTarget(expression.leftExpression),
                right = buildTarget(expression.rightExpression),
                withParentheses = withParentheses
            )
            is Parenthesis -> buildTarget(expression.expression, true)
            is SubSelect -> {
                val subContext = SqlParseContext(expression.selectBody.toString()).apply {
                    type = SqlType.QUERY
                }
                expression.selectBody.accept(subContext.visitor.select)
                SqlTarget.SubSelect(Sql.Select.fromContext(subContext), true)
            }
            is LongValue -> SqlTarget.LongLiteral(expression.value)
            is StringValue -> SqlTarget.StringLiteral(expression.value)
            is DoubleValue -> SqlTarget.DoubleLiteral(expression.value)
            is SignedExpression -> {
                val e = buildTarget(expression.expression)
                val sign = if (expression.sign == '-') {
                    -1
                } else {
                    1
                }
                when (e) {
                    is SqlTarget.LongLiteral -> e.copy(value = sign * e.value)
                    is SqlTarget.DoubleLiteral -> e.copy(value = sign * e.value)
                    else -> throw IllegalArgumentException(
                        "can't build an ${SqlTarget::class.simpleName} from expression of type "
                        + "${expression::class.qualifiedName} ($expression) - expected it to resolve into numeric "
                        + "literal but got ${e::class.qualifiedName} ($e)"
                    )
                }
            }
            is JdbcParameter -> SqlTarget.Placeholder("?")
            is DateTimeLiteralExpression -> SqlTarget.DateTimeLiteral(expression.value, expression.type)
            else -> throw UnsupportedOperationException(
                "can't parse expression of type ${expression::class.qualifiedName} ($expression)"
            )
        }
    }

    private fun extractFunctionOperands(function: net.sf.jsqlparser.expression.Function): List<SqlTarget> {
        val parameters = function.parameters
        return if (parameters == null) {
            if (function.isAllColumns) {
                listOf(SqlTarget.AllColumns)
            } else {
                emptyList()
            }
        } else {
            buildFunctionOperands {
                for (operand in parameters.expressions) {
                    operand.accept(visitor.expression)
                }
            }
        }
    }

    fun addTable(name: String, alias: String?, schemaName: String?) {
        when {
            parsingJoin -> joinTable = Table(name, alias, schemaName)
            else -> this.table = Table(name, alias, schemaName)
        }
    }

    fun addOperation(operationType: String, select: Sql.Select) {
        operations.add(Operation(operationType, select))
    }

    fun addSubSelect(subSelect: Sql.Select, subSelectAlias: String?) {
        this.subSelect = subSelect
        this.subSelectAlias = subSelectAlias
    }

    class Visitors(context: SqlParseContext) {
        val statement = ParsingStatementVisitor(context)
        val expression = ParsingExpressionVisitor(context)
        val select = ParsingSelectVisitor(context)
        val selectItem = ParsingSelectItemVisitor(context)
        val fromItem = ParsingFromItemVisitor(context)
        val groupBy = ParsingGroupByVisitor(context)
        val orderBy = ParsingOrderByVisitor(context)
    }

    private data class OngoingFilter(val and: Boolean) {
        val filters = mutableListOf<Filter>()
    }
}