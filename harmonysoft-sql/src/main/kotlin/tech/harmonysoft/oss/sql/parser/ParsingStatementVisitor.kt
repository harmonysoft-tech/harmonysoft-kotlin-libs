package tech.harmonysoft.oss.sql.parser

import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.select.Select
import tech.harmonysoft.oss.sql.ast.delegation.DelegatingStatementVisitorAdapter

class ParsingStatementVisitor(
    private val context: SqlParseContext
) : DelegatingStatementVisitorAdapter() {

    override fun handle(statement: Statement) {
        throw IllegalArgumentException("unexpected statement of class ${statement::class.qualifiedName}: $statement")
    }

    override fun visit(select: Select) {
        context.type = SqlType.QUERY
        select.selectBody.accept(context.visitor.select)
    }
}