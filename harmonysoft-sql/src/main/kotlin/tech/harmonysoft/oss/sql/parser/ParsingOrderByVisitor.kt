package tech.harmonysoft.oss.sql.parser

import net.sf.jsqlparser.statement.select.OrderByElement
import net.sf.jsqlparser.statement.select.OrderByVisitor
import tech.harmonysoft.oss.sql.dsl.orderby.OrderBy

class ParsingOrderByVisitor(
    private val context: SqlParseContext
) : OrderByVisitor {

    override fun visit(orderBy: OrderByElement) {
        context.orderBy.add(OrderBy(context.buildTarget(orderBy.expression), orderBy.isAsc))
    }
}