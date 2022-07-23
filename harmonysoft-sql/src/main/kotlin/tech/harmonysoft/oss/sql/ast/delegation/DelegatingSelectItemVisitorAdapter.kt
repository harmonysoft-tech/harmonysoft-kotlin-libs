package tech.harmonysoft.oss.sql.ast.delegation

import net.sf.jsqlparser.statement.select.*

abstract class DelegatingSelectItemVisitorAdapter : SelectItemVisitor {
    
    abstract fun handle(item: SelectItem)

    override fun visit(allColumns: AllColumns) {
        handle(allColumns)
    }

    override fun visit(allTableColumns: AllTableColumns) {
        handle(allTableColumns)
    }

    override fun visit(selectExpressionItem: SelectExpressionItem) {
        handle(selectExpressionItem)
    }
}