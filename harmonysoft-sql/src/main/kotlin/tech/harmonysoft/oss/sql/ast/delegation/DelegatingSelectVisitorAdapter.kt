package tech.harmonysoft.oss.sql.ast.delegation

import net.sf.jsqlparser.statement.select.*
import net.sf.jsqlparser.statement.values.ValuesStatement

abstract class DelegatingSelectVisitorAdapter : SelectVisitor {
    
    abstract fun handle(select: SelectBody)

    override fun visit(plainSelect: PlainSelect) {
        handle(plainSelect)
    }

    override fun visit(setOpList: SetOperationList) {
        handle(setOpList)
    }

    override fun visit(withItem: WithItem) {
        handle(withItem)
    }

    override fun visit(aThis: ValuesStatement) {
        handle(aThis)
    }
}