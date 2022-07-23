package tech.harmonysoft.oss.sql.parser

import net.sf.jsqlparser.statement.select.AllColumns
import net.sf.jsqlparser.statement.select.SelectExpressionItem
import net.sf.jsqlparser.statement.select.SelectItem
import tech.harmonysoft.oss.sql.ast.delegation.DelegatingSelectItemVisitorAdapter
import tech.harmonysoft.oss.sql.dsl.target.SelectTarget
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget

class ParsingSelectItemVisitor(
    private val context: SqlParseContext
) : DelegatingSelectItemVisitorAdapter() {

    override fun handle(item: SelectItem) {
        throw IllegalArgumentException("unexpected select item of class ${item::class.qualifiedName}: $item")
    }

    override fun visit(selectExpressionItem: SelectExpressionItem) {
        val target = context.buildTarget(selectExpressionItem.expression)
        context.targets += SelectTarget(target, selectExpressionItem.alias?.name?.trim())
    }

    override fun visit(allColumns: AllColumns) {
        context.targets += SelectTarget(SqlTarget.AllColumns)
    }
}