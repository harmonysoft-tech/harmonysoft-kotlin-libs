package tech.harmonysoft.oss.sql.ast.delegation

import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.select.*

abstract class DelegatingFromItemVisitorAdapter : FromItemVisitor {
    
    abstract fun handle(item: FromItem)

    override fun visit(table: Table) {
        handle(table)
    }

    override fun visit(subSelect: SubSelect) {
        handle(subSelect)
    }

    override fun visit(subjoin: SubJoin) {
        handle(subjoin)
    }

    override fun visit(lateralSubSelect: LateralSubSelect) {
        handle(lateralSubSelect)
    }

    override fun visit(valuesList: ValuesList) {
        handle(valuesList)
    }

    override fun visit(tableFunction: TableFunction) {
        handle(tableFunction)
    }

    override fun visit(aThis: ParenthesisFromItem) {
        handle(aThis)
    }
}