package tech.harmonysoft.oss.sql.ast.delegation

import net.sf.jsqlparser.expression.*
import net.sf.jsqlparser.expression.Function
import net.sf.jsqlparser.expression.operators.arithmetic.*
import net.sf.jsqlparser.expression.operators.conditional.AndExpression
import net.sf.jsqlparser.expression.operators.conditional.OrExpression
import net.sf.jsqlparser.expression.operators.conditional.XorExpression
import net.sf.jsqlparser.expression.operators.relational.*
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.statement.select.AllColumns
import net.sf.jsqlparser.statement.select.AllTableColumns
import net.sf.jsqlparser.statement.select.SubSelect

abstract class DelegatingExpressionVisitorAdapter : ExpressionVisitor {
    
    abstract fun handleExpression(expression: Expression)

    override fun visit(aThis: BitwiseRightShift) {
        handleExpression(aThis)
    }

    override fun visit(aThis: BitwiseLeftShift) {
        handleExpression(aThis)
    }

    override fun visit(nullValue: NullValue) {
        handleExpression(nullValue)
    }

    override fun visit(function: Function) {
        handleExpression(function)
    }

    override fun visit(signedExpression: SignedExpression) {
        handleExpression(signedExpression)
    }

    override fun visit(jdbcParameter: JdbcParameter) {
        handleExpression(jdbcParameter)
    }

    override fun visit(jdbcNamedParameter: JdbcNamedParameter) {
        handleExpression(jdbcNamedParameter)
    }

    override fun visit(doubleValue: DoubleValue) {
        handleExpression(doubleValue)
    }

    override fun visit(longValue: LongValue) {
        handleExpression(longValue)
    }

    override fun visit(hexValue: HexValue) {
        handleExpression(hexValue)
    }

    override fun visit(dateValue: DateValue) {
        handleExpression(dateValue)
    }

    override fun visit(timeValue: TimeValue) {
        handleExpression(timeValue)
    }

    override fun visit(timestampValue: TimestampValue) {
        handleExpression(timestampValue)
    }

    override fun visit(parenthesis: Parenthesis) {
        handleExpression(parenthesis)
    }

    override fun visit(stringValue: StringValue) {
        handleExpression(stringValue)
    }

    override fun visit(addition: Addition) {
        handleExpression(addition)
    }

    override fun visit(division: Division) {
        handleExpression(division)
    }

    override fun visit(division: IntegerDivision) {
        handleExpression(division)
    }

    override fun visit(multiplication: Multiplication) {
        handleExpression(multiplication)
    }

    override fun visit(subtraction: Subtraction) {
        handleExpression(subtraction)
    }

    override fun visit(andExpression: AndExpression) {
        handleExpression(andExpression)
    }

    override fun visit(orExpression: OrExpression) {
        handleExpression(orExpression)
    }

    override fun visit(orExpression: XorExpression) {
        handleExpression(orExpression)
    }

    override fun visit(between: Between) {
        handleExpression(between)
    }

    override fun visit(equalsTo: EqualsTo) {
        handleExpression(equalsTo)
    }

    override fun visit(greaterThan: GreaterThan) {
        handleExpression(greaterThan)
    }

    override fun visit(greaterThanEquals: GreaterThanEquals) {
        handleExpression(greaterThanEquals)
    }

    override fun visit(inExpression: InExpression) {
        handleExpression(inExpression)
    }

    override fun visit(fullTextSearch: FullTextSearch) {
        handleExpression(fullTextSearch)
    }

    override fun visit(isNullExpression: IsNullExpression) {
        handleExpression(isNullExpression)
    }

    override fun visit(isBooleanExpression: IsBooleanExpression) {
        handleExpression(isBooleanExpression)
    }

    override fun visit(likeExpression: LikeExpression) {
        handleExpression(likeExpression)
    }

    override fun visit(minorThan: MinorThan) {
        handleExpression(minorThan)
    }

    override fun visit(minorThanEquals: MinorThanEquals) {
        handleExpression(minorThanEquals)
    }

    override fun visit(notEqualsTo: NotEqualsTo) {
        handleExpression(notEqualsTo)
    }

    override fun visit(tableColumn: Column) {
        handleExpression(tableColumn)
    }

    override fun visit(subSelect: SubSelect) {
        handleExpression(subSelect)
    }

    override fun visit(caseExpression: CaseExpression) {
        handleExpression(caseExpression)
    }

    override fun visit(whenClause: WhenClause) {
        handleExpression(whenClause)
    }

    override fun visit(existsExpression: ExistsExpression) {
        handleExpression(existsExpression)
    }

    override fun visit(anyComparisonExpression: AnyComparisonExpression) {
        handleExpression(anyComparisonExpression)
    }

    override fun visit(concat: Concat) {
        handleExpression(concat)
    }

    override fun visit(matches: Matches) {
        handleExpression(matches)
    }

    override fun visit(bitwiseAnd: BitwiseAnd) {
        handleExpression(bitwiseAnd)
    }

    override fun visit(bitwiseOr: BitwiseOr) {
        handleExpression(bitwiseOr)
    }

    override fun visit(bitwiseXor: BitwiseXor) {
        handleExpression(bitwiseXor)
    }

    override fun visit(cast: CastExpression) {
        handleExpression(cast)
    }

    override fun visit(cast: TryCastExpression) {
        handleExpression(cast)
    }

    override fun visit(modulo: Modulo) {
        handleExpression(modulo)
    }

    override fun visit(aexpr: AnalyticExpression) {
        handleExpression(aexpr)
    }

    override fun visit(eexpr: ExtractExpression) {
        handleExpression(eexpr)
    }

    override fun visit(iexpr: IntervalExpression) {
        handleExpression(iexpr)
    }

    override fun visit(oexpr: OracleHierarchicalExpression) {
        handleExpression(oexpr)
    }

    override fun visit(rexpr: RegExpMatchOperator) {
        handleExpression(rexpr)
    }

    override fun visit(jsonExpr: JsonExpression) {
        handleExpression(jsonExpr)
    }

    override fun visit(jsonExpr: JsonOperator) {
        handleExpression(jsonExpr)
    }

    override fun visit(regExpMySQLOperator: RegExpMySQLOperator) {
        handleExpression(regExpMySQLOperator)
    }

    override fun visit(`var`: UserVariable) {
        handleExpression(`var`)
    }

    override fun visit(bind: NumericBind) {
        handleExpression(bind)
    }

    override fun visit(aexpr: KeepExpression) {
        handleExpression(aexpr)
    }

    override fun visit(groupConcat: MySQLGroupConcat) {
        handleExpression(groupConcat)
    }

    override fun visit(valueList: ValueListExpression) {
        handleExpression(valueList)
    }

    override fun visit(rowConstructor: RowConstructor) {
        handleExpression(rowConstructor)
    }

    override fun visit(rowGetExpression: RowGetExpression) {
        handleExpression(rowGetExpression)
    }

    override fun visit(hint: OracleHint) {
        handleExpression(hint)
    }

    override fun visit(timeKeyExpression: TimeKeyExpression) {
        handleExpression(timeKeyExpression)
    }

    override fun visit(literal: DateTimeLiteralExpression) {
        handleExpression(literal)
    }

    override fun visit(aThis: NotExpression) {
        handleExpression(aThis)
    }

    override fun visit(aThis: NextValExpression) {
        handleExpression(aThis)
    }

    override fun visit(aThis: CollateExpression) {
        handleExpression(aThis)
    }

    override fun visit(aThis: SimilarToExpression) {
        handleExpression(aThis)
    }

    override fun visit(aThis: ArrayExpression) {
        handleExpression(aThis)
    }

    override fun visit(aThis: ArrayConstructor) {
        handleExpression(aThis)
    }

    override fun visit(aThis: VariableAssignment) {
        handleExpression(aThis)
    }

    override fun visit(aThis: XMLSerializeExpr) {
        handleExpression(aThis)
    }

    override fun visit(aThis: TimezoneExpression) {
        handleExpression(aThis)
    }

    override fun visit(aThis: JsonAggregateFunction) {
        handleExpression(aThis)
    }

    override fun visit(aThis: JsonFunction) {
        handleExpression(aThis)
    }

    override fun visit(aThis: ConnectByRootOperator) {
        handleExpression(aThis)
    }

    override fun visit(aThis: OracleNamedFunctionParameter) {
        handleExpression(aThis)
    }

    override fun visit(allColumns: AllColumns) {
        handleExpression(allColumns)
    }

    override fun visit(allTableColumns: AllTableColumns) {
        handleExpression(allTableColumns)
    }

    override fun visit(allValue: AllValue) {
        handleExpression(allValue)
    }

    override fun visit(isDistinctExpression: IsDistinctExpression) {
        handleExpression(isDistinctExpression)
    }

    override fun visit(geometryDistance: GeometryDistance) {
        handleExpression(geometryDistance)
    }
}