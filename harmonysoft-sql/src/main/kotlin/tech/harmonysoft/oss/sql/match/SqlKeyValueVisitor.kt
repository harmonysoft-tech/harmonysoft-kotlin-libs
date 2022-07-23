package tech.harmonysoft.oss.sql.match

import tech.harmonysoft.oss.sql.match.impl.*

interface SqlKeyValueVisitor {
    fun visit(matcher: And)
    fun visit(matcher: Or)
    fun visit(matcher: IsNull)
    fun visit(matcher: Between)
    fun visit(matcher: In)
    fun visit(matcher: Like)
    fun visit(matcher: Le)
    fun visit(matcher: Lt)
    fun visit(matcher: Ge)
    fun visit(matcher: Gt)
    fun visit(matcher: Eq)
    fun visit(matcher: Not)
    fun visit(matcher: MatchNoneMatcher)
    fun visit(matcher: MatchAllMatcher)
}