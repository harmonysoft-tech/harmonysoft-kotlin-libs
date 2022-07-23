package tech.harmonysoft.oss.sql.parser

import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.select.FromItem
import net.sf.jsqlparser.statement.select.SubSelect
import tech.harmonysoft.oss.sql.ast.delegation.DelegatingFromItemVisitorAdapter
import tech.harmonysoft.oss.sql.dsl.Sql

class ParsingFromItemVisitor(
    private val context: SqlParseContext
) : DelegatingFromItemVisitorAdapter() {

    override fun handle(item: FromItem) {
        throw IllegalArgumentException("unexpected from item of class ${item::class.qualifiedName}: $item")
    }

    override fun visit(table: Table) {
        context.addTable(table.name, table.alias?.name, table.schemaName)
    }

    override fun visit(subSelect: SubSelect) {
        val subContext = SqlParseContext(subSelect.toString())
        subContext.type = SqlType.QUERY
        subSelect.selectBody.accept(subContext.visitor.select)
        context.addSubSelect(Sql.Select.fromContext(subContext), subSelect.alias?.name)
    }
}