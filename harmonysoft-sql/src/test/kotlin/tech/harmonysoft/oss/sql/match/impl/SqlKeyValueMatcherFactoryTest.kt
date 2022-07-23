package tech.harmonysoft.oss.sql.match.impl

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import tech.harmonysoft.oss.common.data.DataProviderStrategy
import tech.harmonysoft.oss.common.data.TypedKeyManager
import tech.harmonysoft.oss.common.spring.AbstractSpringTest
import tech.harmonysoft.oss.common.string.util.StringUtil
import tech.harmonysoft.oss.common.type.TypeManagerContext
import tech.harmonysoft.oss.di.CommonTestConfig
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget
import tech.harmonysoft.oss.sql.parser.SqlParser
import tech.harmonysoft.oss.sql.type.SqlTypeManagerContext
import tech.harmonysoft.oss.test.util.TestUtil.fail
import java.math.BigDecimal
import kotlin.reflect.KClass

@ContextConfiguration(classes = [CommonTestConfig::class])
internal class SqlKeyValueMatcherFactoryTest : AbstractSpringTest() {

    @Autowired private lateinit var factory: SqlKeyValueMatcherFactory
    @Autowired private lateinit var parser: SqlParser

    private val keyManager = object : TypedKeyManager<SqlTarget.Column> {

        override fun getValueType(key: SqlTarget.Column): KClass<*> {
            val columnName = key.name
            return when {
                columnName.startsWith("s") -> String::class
                columnName.startsWith("i") -> Int::class
                columnName.startsWith("d") -> Double::class
                columnName.startsWith("bd") -> BigDecimal::class
                else -> throw IllegalArgumentException("unexpected column name '$columnName'")
            }
        }

        override fun parseKey(raw: String): SqlTarget.Column {
            return parseSqlTarget(raw)
        }
    }

    private fun parseSqlTarget(s: String): SqlTarget.Column {
        return (parser.parseSelect("select $s from t")).columns.first().target as SqlTarget.Column
    }

    private fun doTest(
        rules: Map<String/* rule id */, String/* rule definition */>,
        actualData: Map<String, Any?>,
        vararg expectedRulesToMatch: String
    ) {
        val matchersById = rules.mapValues {
            factory.build(it.value, keyManager, TypeManagerContext.DEFAULT, SqlTypeManagerContext.INSTANCE)
        }
        val actualDataWithParsedKeys = actualData.mapKeys { parseSqlTarget(it.key) }
        for ((id, matcher) in matchersById) {
            val actual = matcher.matches(actualDataWithParsedKeys, DataProviderStrategy.fromMap())
            val expected = expectedRulesToMatch.contains(id)
            if (actual != expected) {
                fail("expected rule '${rules[id]}' to ${if (expected) "" else "not "} match data $actualData "
                     + "but it ${if (actual) "matches" else "does not match"}")
            }
        }
    }

    @Test
    fun `when no value is available then matcher works as expected`() {
        doTest(
            mapOf(
                "1" to "s <> 'B'",
                "2" to "s not in ('XXX')",
                "3" to "s not like 'S%'"
            ),
            emptyMap(),
            "1", "2", "3"
        )
    }

    @Test
    fun `when EQ is used then matcher works as expected`() {
        doTest(
            mapOf(
                "1" to "s = 'B'",
                "2" to "s = 'XXX'"
            ),
            mapOf("s" to "B"),
            "1"
        )
    }

    @Test
    fun `when EQ is used with other fields then matcher works as expected`() {
        doTest(
            mapOf(
                "1" to "s = o",
                "2" to "s != o"
            ),
            mapOf("s" to "B", "o" to "B"),
            "1"
        )
    }

    @Test
    fun `when empty string is used then matcher works as expected`() {
        doTest(
            mapOf(
                "1" to "s = ''",
                "2" to "s != ''"
            ),
            mapOf("s" to ""),
            "1"
        )
    }

    @Test
    fun `when OR and EQ are used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 = 'CLIENT1' OR s2 = 'B'",
            "2" to "s1 = 'XXX'"
        )
        doTest(selectors, mapOf("s2" to "B"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT1"), "1")
        doTest(selectors, mapOf("s1" to "XYZ"))
        doTest(selectors, mapOf(
            "s1" to "XXX",
            "s2" to "B"
        ), "1", "2")
    }

    @Test
    fun `when AND and EQ are used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 = 'CLIENT1' AND s2 = 'B'",
            "2" to "s1 = 'CLIENT1'"
        )
        doTest(selectors, mapOf("s2" to "B"))
        doTest(selectors, mapOf("s1" to "CLIENT1"), "2")
        doTest(selectors, mapOf("s1" to "XYZ"))
        doTest(selectors, mapOf(
            "s1" to "CLIENT1",
            "s2" to "B"
        ), "1", "2")
    }

    @Test
    fun `when AND inside OR is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 = 'CLIENT1' OR (s2 = 'B' AND s3 = 'ENTITY1')",
            "2" to "s2 = 'B'"
        )
        doTest(selectors, mapOf("s2" to "B"), "2")
        doTest(selectors, mapOf("s1" to "CLIENT1"), "1")
        doTest(selectors, mapOf("s3" to "ENTITY2"))
        doTest(selectors, mapOf(
            "s2" to "B",
            "s3" to "ENTITY1"
        ), "1", "2")
    }

    @Test
    fun `when OR inside AND is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 = 'CLIENT1' AND (s2 = 'B' OR s3 = 'ENTITY1')",
            "2" to "s2 = 'B'"
        )
        doTest(selectors, mapOf("s2" to "B"), "2")
        doTest(selectors, mapOf("s1" to "CLIENT1"))
        doTest(selectors, mapOf(
            "s1" to "CLIENT1",
            "s2" to "B"
        ), "1", "2")
        doTest(selectors, mapOf(
            "s1" to "CLIENT1",
            "s3" to "ENTITY1"
        ), "1")
    }

    @Test
    fun `when LIKE without wildcard is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 LIKE 'CLIENT1'",
            "2" to "s1 = 'CLIENT2'"
        )
        doTest(selectors, mapOf("s1" to "CLIENT1"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT2"), "2")
        doTest(selectors, mapOf("s1" to "CLIENT3"))
        doTest(selectors, mapOf("s2" to "B"))
    }

    @Test
    fun `when NOT LIKE without wildcard is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 NOT LIKE 'CLIENT1'",
            "2" to "s1 = 'CLIENT2'"
        )
        doTest(selectors, mapOf("s1" to "CLIENT1"))
        doTest(selectors, mapOf("s1" to "CLIENT2"), "1", "2")
        doTest(selectors, mapOf("s1" to "CLIENT3"), "1")
        doTest(selectors, mapOf("s2" to "B"), "1")
    }

    @Test
    fun `when LIKE with wildcard in the beginning is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 LIKE '%CLIENT'",
            "2" to "s1 = '2CLIENT'"
        )
        doTest(selectors, mapOf("s1" to "CLIENT"), "1")
        doTest(selectors, mapOf("s1" to "1CLIENT"), "1")
        doTest(selectors, mapOf("s1" to "2CLIENT"), "1", "2")
        doTest(selectors, mapOf("s1" to "21CLIENT"), "1")
        doTest(selectors, mapOf("s2" to "B"))
    }

    @Test
    fun `when NOT LIKE with wildcard in the beginning is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 NOT LIKE '%CLIENT'",
            "2" to "s1 = '2CLIENT'"
        )
        doTest(selectors, mapOf("s1" to "CLIENT"))
        doTest(selectors, mapOf("s1" to "1CLIENT"))
        doTest(selectors, mapOf("s1" to "2CLIENT"), "2")
        doTest(selectors, mapOf("s1" to "LIENT"), "1")
        doTest(selectors, mapOf("s2" to "B"), "1")
    }

    @Test
    fun `when LIKE with wildcard in the end is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 LIKE 'CLIENT%'",
            "2" to "s1 = 'CLIENT2'"
        )
        doTest(selectors, mapOf("s1" to "CLIENT"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT1"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT2"), "1", "2")
        doTest(selectors, mapOf("s1" to "CLIENT21"), "1")
        doTest(selectors, mapOf("s2" to "B"))
    }

    @Test
    fun `when NOT LIKE with wildcard in the end is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 NOT LIKE 'CLIENT%'",
            "2" to "s1 = 'CLIENT2'"
        )
        doTest(selectors, mapOf("s1" to "CLIENT"))
        doTest(selectors, mapOf("s1" to "CLIENT1"))
        doTest(selectors, mapOf("s1" to "CLIENT2"), "2")
        doTest(selectors, mapOf("s1" to "CLIEN"), "1")
        doTest(selectors, mapOf("s2" to "B"), "1")
    }

    @Test
    fun `when LIKE with wildcard in the middle is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 LIKE 'C%T'",
            "2" to "s1 = 'CLIENT'"
        )
        doTest(selectors, mapOf("s1" to "C"))
        doTest(selectors, mapOf("s1" to "CT"), "1")
        doTest(selectors, mapOf("s1" to "CLT"), "1")
        doTest(selectors, mapOf("s1" to "CLNT"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT"), "1", "2")
        doTest(selectors, mapOf("s2" to "B"))
    }

    @Test
    fun `when NOT LIKE with wildcard in the middle is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 NOT LIKE 'C%T'",
            "2" to "s1 = 'CLIENT'"
        )
        doTest(selectors, mapOf("s1" to "C"), "1")
        doTest(selectors, mapOf("s1" to "CT"))
        doTest(selectors, mapOf("s1" to "CLT"))
        doTest(selectors, mapOf("s1" to "CLNT"))
        doTest(selectors, mapOf("s1" to "CLIENT"), "2")
        doTest(selectors, mapOf("s2" to "B"), "1")
    }

    @Test
    fun `when LIKE is used then it is regex-escaped`() {
        val selectors = mapOf(
            "1" to "s1 LIKE 'CLIENT.%'",
            "2" to "s1 = 'CLIENT.2'"
        )
        doTest(selectors, mapOf("s1" to "CLIENT"))
        doTest(selectors, mapOf("s1" to "CLIENT1"))
        doTest(selectors, mapOf("s1" to "CLIENT.1"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT.2"), "1", "2")
        doTest(selectors, mapOf("s1" to "CLIENT.12"), "1")
        doTest(selectors, mapOf("s2" to "B"))
    }

    @Test
    fun `when NOT LIKE is used then it is regex-escaped`() {
        val selectors = mapOf(
            "1" to "s1 NOT LIKE 'CLIENT.%'",
            "2" to "s1 = 'CLIENT.2'"
        )
        doTest(selectors, mapOf("s1" to "CLIENT"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT1"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT.1"))
        doTest(selectors, mapOf("s1" to "CLIENT.2"), "2")
        doTest(selectors, mapOf("s1" to "CLIENT.12"))
        doTest(selectors, mapOf("s2" to "B"), "1")
    }

    @Test
    fun `when NOT EQ is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 <> 'CLIENT1'",
            "2" to "s1 = 'CLIENT2'"
        )
        doTest(selectors, mapOf("s1" to "CLIENT"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT1"))
        doTest(selectors, mapOf("s1" to "CLIENT2"), "1", "2")
        doTest(selectors, mapOf("s2" to "B"), "1")
    }

    @Test
    fun `when NOT EQ is used with other fields then matcher works as expected`() {
        doTest(
            mapOf(
                "1" to "s = o",
                "2" to "s <> o"
            ),
            mapOf("s" to "B", "o" to "S"),
            "2"
        )
    }

    @Test
    fun `when NOT EQ with LIKE-matching string is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s1 <> 'CLIENT%'",
            "2" to "s1 = 'CLIENT2'"
        )
        doTest(selectors, mapOf("s1" to "CLIENT%"))
        doTest(selectors, mapOf("s1" to "CLIENT"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT1"), "1")
        doTest(selectors, mapOf("s1" to "CLIENT2"), "1", "2")
    }

    @Test
    fun `when BigDecimal is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "bd = 10.0",
            "2" to "bd = 11"
        )
        doTest(selectors, mapOf("bd" to BigDecimal.valueOf(10L)), "1")
        doTest(selectors, mapOf("bd" to BigDecimal.valueOf(10.0)), "1")
        doTest(selectors, mapOf("bd" to BigDecimal.valueOf(11L)), "2")
        doTest(selectors, mapOf("bd" to BigDecimal.valueOf(11.0)), "2")
        doTest(selectors, mapOf("bd" to BigDecimal.valueOf(13)))
        doTest(selectors, emptyMap())
    }

    @Test
    fun `when BigDecimal is used in NOT EQ then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "bd <> 10.0",
            "2" to "bd != 11"
        )
        doTest(selectors, mapOf("bd" to BigDecimal.valueOf(10L)), "2")
        doTest(selectors, mapOf("bd" to BigDecimal.valueOf(10.0)), "2")
        doTest(selectors, mapOf("bd" to BigDecimal.valueOf(11L)), "1")
        doTest(selectors, mapOf("bd" to BigDecimal.valueOf(11.0)), "1")
        doTest(selectors, mapOf("bd" to BigDecimal.valueOf(13)), "1", "2")
        doTest(selectors, emptyMap(), "1", "2")
    }

    @Test
    fun `when int is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "i = 1",
            "2" to "i <> 2"
        )
        doTest(selectors, mapOf("i" to 0), "2")
        doTest(selectors, mapOf("i" to 1), "1", "2")
        doTest(selectors, mapOf("i" to 2))
    }

    @Test
    fun `when double is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "d = 10",
            "2" to "d = 10.0",
            "3" to "d <> 11",
            "4" to "d != 11.0"
        )
        doTest(selectors, mapOf("d" to 10.0), "1", "2", "3", "4")
        doTest(selectors, mapOf("d" to 11.0))
        doTest(selectors, mapOf("d" to 12.0), "3", "4")
    }

    @Test
    fun `when GT is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "i > 10"
        )
        doTest(selectors, mapOf("i" to 9))
        doTest(selectors, mapOf("i" to 10))
        doTest(selectors, mapOf("i" to 11), "1")
    }

    @Test
    fun `when GT with other field is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "i > o"
        )
        doTest(selectors, mapOf("i" to 0, "o" to 1))
        doTest(selectors, mapOf("i" to 1, "o" to 1))
        doTest(selectors, mapOf("i" to 2, "o" to 1), "1")
    }

    @Test
    fun `when GE is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "i >= 10"
        )
        doTest(selectors, mapOf("i" to 9))
        doTest(selectors, mapOf("i" to 10), "1")
        doTest(selectors, mapOf("i" to 11), "1")
    }

    @Test
    fun `when GE with other field is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "i >= o"
        )
        doTest(selectors, mapOf("i" to 0, "o" to 1))
        doTest(selectors, mapOf("i" to 1, "o" to 1), "1")
        doTest(selectors, mapOf("i" to 2, "o" to 1), "1")
    }

    @Test
    fun `when LT is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "i < 10"
        )
        doTest(selectors, mapOf("i" to 9), "1")
        doTest(selectors, mapOf("i" to 10))
        doTest(selectors, mapOf("i" to 11))
    }

    @Test
    fun `when LT with other field is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "i < o"
        )
        doTest(selectors, mapOf("i" to 0, "o" to 1), "1")
        doTest(selectors, mapOf("i" to 1, "o" to 1))
        doTest(selectors, mapOf("i" to 2, "o" to 1))
    }

    @Test
    fun `when LE is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "i <= 10"
        )
        doTest(selectors, mapOf("i" to 9), "1")
        doTest(selectors, mapOf("i" to 10), "1")
        doTest(selectors, mapOf("i" to 11))
    }

    @Test
    fun `when LE with other field is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "i <= o"
        )
        doTest(selectors, mapOf("i" to 0, "o" to 1), "1")
        doTest(selectors, mapOf("i" to 1, "o" to 1), "1")
        doTest(selectors, mapOf("i" to 2, "o" to 1))
    }

    @Test
    fun `when IN is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s IN ('A', 'B')",
            "2" to "s IN ('B')"
        )
        doTest(selectors, mapOf("s" to "A"), "1")
        doTest(selectors, mapOf("s" to "B"), "1", "2")
        doTest(selectors, mapOf("s" to "C"))
        doTest(selectors, emptyMap())
    }

    @Test
    fun `when NOT IN is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s NOT IN ('A', 'B')",
            "2" to "s NOT IN ('B')"
        )
        doTest(selectors, mapOf("s" to "A"), "2")
        doTest(selectors, mapOf("s" to "B"))
        doTest(selectors, mapOf("s" to "C"), "1", "2")
        doTest(selectors, emptyMap(), "1", "2")
    }

    @Test
    fun `when BETWEEN is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "i BETWEEN 2 AND 4",
            "2" to "i BETWEEN -1 and 2"
        )
        doTest(selectors, mapOf("i" to -2))
        doTest(selectors, mapOf("i" to -1), "2")
        doTest(selectors, mapOf("i" to 0), "2")
        doTest(selectors, mapOf("i" to 2), "1", "2")
        doTest(selectors, mapOf("i" to 3), "1")
        doTest(selectors, mapOf("i" to 4), "1")
        doTest(selectors, mapOf("i" to 5))
        doTest(selectors, emptyMap())
    }

    @Test
    fun `when IS NULL is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s IS NULL"
        )
        doTest(selectors, emptyMap(), "1")
        doTest(selectors, mapOf("s" to "abc"))
    }

    @Test
    fun `when IS NOT NULL is used then matcher works as expected`() {
        val selectors = mapOf(
            "1" to "s IS NOT NULL"
        )
        doTest(selectors, emptyMap())
        doTest(selectors, mapOf("s" to "abc"), "1")
    }

    @Test
    fun `when NOT with composite criteria is used then matcher works as expected`() {
        val selector = StringUtil.toSingleLine("""
            not (
              s1 = 'H'
              and s2 not like '%.SH'
              and s2 not like '%.ZK'
            )
        """)
        val selectors = mapOf("1" to selector)
        doTest(selectors, emptyMap(), "1")
        doTest(selectors, mapOf("s1" to "H"))
        doTest(selectors, mapOf("s2" to "1.SH"), "1")
        doTest(selectors, mapOf("s2" to "1.ZK"), "1")
        doTest(selectors, mapOf(
            "s1" to "H",
            "s2" to "SH"
        ))
        doTest(selectors, mapOf(
            "s1" to "H",
            "s2" to "1.ZK"
        ), "1")
    }
}