package tech.harmonysoft.oss.sql.dsl.target

interface SqlTargetVisitor<T> {

    fun visit(target: SqlTarget.AllColumns): T

    fun visit(target: SqlTarget.Column): T

    fun visit(target: SqlTarget.Function): T

    fun visit(target: SqlTarget.OperatorFunction): T

    fun visit(target: SqlTarget.LongLiteral): T

    fun visit(target: SqlTarget.StringLiteral): T

    fun visit(target: SqlTarget.DoubleLiteral): T

    fun visit(target: SqlTarget.Placeholder): T

    fun visit(target: SqlTarget.SubSelect): T

    fun visit(target: SqlTarget.DateTimeLiteral): T
}