package tech.harmonysoft.oss.sql.parser

import net.sf.jsqlparser.parser.CCJSqlParserUtil
import tech.harmonysoft.oss.common.string.util.StringUtil
import tech.harmonysoft.oss.sql.dsl.Sql
import tech.harmonysoft.oss.sql.dsl.filter.Filter
import javax.inject.Named

@Named
class SqlParser {

    fun parseSelect(sql: String): Sql.Select {
        val result = parse(sql)
        return result as? Sql.Select ?: throw IllegalArgumentException(
            "expected to parse the following sql as 'select' but got ${result::class.qualifiedName}: $sql"
        )
    }

    fun parse(sql: String): Sql {
        val context = SqlParseContext(StringUtil.toSingleLine(sql))
        CCJSqlParserUtil.parse(context.sql).accept(context.visitor.statement)
        return Sql.Select.fromContext(context)
    }

    fun parseFilter(filter: String): Filter {
        val sql = parseSelect("select x from xxx where ${StringUtil.toSingleLine(filter)}")
        return sql.filter ?: throw IllegalArgumentException("can't parse sql filter from '$filter'")
    }
}