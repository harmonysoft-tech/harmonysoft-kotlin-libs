package tech.harmonysoft.oss.sql.match

import tech.harmonysoft.oss.common.match.KeyValueMatcher
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget

interface SqlKeyValueMatcher : KeyValueMatcher<SqlTarget.Column> {

    fun accept(visitor: SqlKeyValueVisitor)
}