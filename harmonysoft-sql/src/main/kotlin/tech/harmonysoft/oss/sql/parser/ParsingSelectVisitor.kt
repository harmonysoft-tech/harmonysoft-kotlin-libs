package tech.harmonysoft.oss.sql.parser

import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.select.PlainSelect
import net.sf.jsqlparser.statement.select.SelectBody
import net.sf.jsqlparser.statement.select.SetOperationList
import tech.harmonysoft.oss.sql.ast.delegation.DelegatingSelectVisitorAdapter
import tech.harmonysoft.oss.sql.dsl.Sql

class ParsingSelectVisitor(
    private val context: SqlParseContext
) : DelegatingSelectVisitorAdapter() {

    override fun handle(select: SelectBody) {
        throw IllegalArgumentException("unexpected select of class ${select::class.qualifiedName}: $select")
    }

    override fun visit(setOpList: SetOperationList) {
        setOpList.selects.first().accept(context.visitor.select)
        setOpList.selects.drop(1).forEachIndexed { index, select ->
            val opContext = SqlParseContext(select.toString())
            CCJSqlParserUtil.parse(opContext.sql).accept(opContext.visitor.statement)
            context.addOperation(setOpList.operations[index].toString(), Sql.Select.fromContext(opContext))
        }
    }

    override fun visit(plainSelect: PlainSelect) {
        plainSelect.distinct?.let { context.distinct = true }
        plainSelect.top?.let { context.top = it.expression.toString().toInt() }
        for (item in plainSelect.selectItems) {
            item.accept(context.visitor.selectItem)
        }
        plainSelect.fromItem.accept(context.visitor.fromItem)
        plainSelect.joins?.forEach {
            context.parseJoin(it) {
                it.rightItem.accept(context.visitor.fromItem)
                it.onExpressions?.forEach { e -> e.accept(context.visitor.expression) }
            }
        }
        plainSelect.where?.accept(context.visitor.expression)
        plainSelect.groupBy?.groupByExpressionList?.expressions?.forEach { it.accept(context.visitor.groupBy) }
        plainSelect.having?.let {
            context.parseHaving {
                it.accept(context.visitor.expression)
            }
        }
        plainSelect.orderByElements?.forEach { it.accept(context.visitor.orderBy) }
    }
}