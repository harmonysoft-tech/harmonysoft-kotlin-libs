package tech.harmonysoft.oss.sql.dsl.filter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.harmonysoft.oss.sql.parser.SqlParser

class SqlDslTest {

    private val parser = SqlParser()

    @Test
    fun `when columns are replaced then it works as expected`() {
        val sql = parser.parse(
            "select a, b alias from t where a = 1 or (a < 10 and b > c) group by b order by a desc"
        )
        val converted = sql.replaceColumns(mapOf("a" to "x", "b" to "y"))
        assertThat(converted.sql).isEqualTo(
            "select x, y alias from t where (x = 1 or (x < 10 and y > c)) group by y order by x desc"
        )
    }
}