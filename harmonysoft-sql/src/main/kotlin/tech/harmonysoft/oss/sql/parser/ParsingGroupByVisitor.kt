package tech.harmonysoft.oss.sql.parser

import net.sf.jsqlparser.expression.Expression
import net.sf.jsqlparser.schema.Column
import tech.harmonysoft.oss.sql.ast.delegation.DelegatingExpressionVisitorAdapter

class ParsingGroupByVisitor(
    private val context: SqlParseContext
) : DelegatingExpressionVisitorAdapter() {

    override fun handleExpression(expression: Expression) {
        throw IllegalArgumentException(
            "unexpected group by expression of class ${expression::class.qualifiedName}: $expression"
        )
    }

    override fun visit(tableColumn: Column) {
        context.groupBy.add(context.buildTarget(tableColumn))
    }
}