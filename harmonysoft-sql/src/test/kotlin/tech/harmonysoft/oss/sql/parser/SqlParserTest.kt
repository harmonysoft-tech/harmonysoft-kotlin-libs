package tech.harmonysoft.oss.sql.parser

import net.sf.jsqlparser.expression.DateTimeLiteralExpression
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.harmonysoft.oss.common.string.util.StringUtil
import tech.harmonysoft.oss.sql.dsl.Sql.Select
import tech.harmonysoft.oss.sql.dsl.constraint.Constraint.*
import tech.harmonysoft.oss.sql.dsl.constraint.Operator
import tech.harmonysoft.oss.sql.dsl.filter.Filter.*
import tech.harmonysoft.oss.sql.dsl.join.Join
import tech.harmonysoft.oss.sql.dsl.join.JoinMethod
import tech.harmonysoft.oss.sql.dsl.operation.Operation
import tech.harmonysoft.oss.sql.dsl.orderby.OrderBy
import tech.harmonysoft.oss.sql.dsl.table.Table
import tech.harmonysoft.oss.sql.dsl.target.SelectTarget
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget.*
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget.Function

internal class SqlParserTest {

    private val parser = SqlParser()

    @Test
    fun `when select with filter is provided then it is correctly parsed`() {
        val raw = "select a, b from c where d = 1"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(
            Select(
                columns = listOf(SelectTarget(Column("a")), SelectTarget(Column("b"))),
                table = Table("c"),
                filter = Leaf(target = Column("d"), constraint = Binary(Operator.EQUAL, LongLiteral(1L)))
            )
        )
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when filter is parsed then it is done correctly`() {
        val raw = "d = 1"
        val filter = parser.parseFilter(raw)
        assertThat(filter).isEqualTo(
            Leaf(
                target = Column("d"),
                constraint = Binary(Operator.EQUAL, LongLiteral(1L))
            )
        )
    }

    @Test
    fun `when select without filter is provided then it is correctly parsed`() {
        val raw = "select a from b"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(
            Select(
                columns = listOf(SelectTarget(Column("a"))),
                table = Table("b")
            )
        )
    }

    @Test
    fun `when table alias is used then it is correctly parsed`() {
        val raw = "select t1.c1, t2.c2 from table1 t1, table2 t2 where t1.c3 = t2.c3"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(
            Select(
                columns = listOf(
                    SelectTarget(Column(name = "c1", table = "t1")),
                    SelectTarget(Column(name = "c2", table = "t2"))
                ),
                table = Table(name = "table1", alias = "t1"),
                joins = listOf(Join(Table(name = "table2", alias = "t2"), null, JoinMethod.SIMPLE)),
                filter = Leaf(
                    target = Column(name = "c3", table = "t1"),
                    constraint = Binary(Operator.EQUAL, Column(name = "c3", table = "t2"))
                )
            )
        )
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when qualified columns with aliases are used then they are correctly parsed`() {
        val raw = "select t1.c1 c11, t2.c2 c22 from table1 t1, table2 t2 where t1.c3 = t2.c3"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(
            Select(
                columns = listOf(
                    SelectTarget(target = Column(name = "c1", table = "t1"), alias = "c11"),
                    SelectTarget(target = Column(name = "c2", table = "t2"), alias = "c22")
                ),
                table = Table(name = "table1", alias = "t1"),
                joins = listOf(Join(Table(name = "table2", alias = "t2"), null, JoinMethod.SIMPLE)),
                filter = Leaf(
                    target = Column(name = "c3", table = "t1"),
                    constraint = Binary(Operator.EQUAL, Column(name = "c3", table = "t2"))
                )
            )
        )
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when not equal filter is used then it is correctly parsed`() {
        val raw = "select c1 from t1 where c2 != c3"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(
            Select(
                columns = listOf(SelectTarget(Column("c1"))),
                table = Table("t1"),
                filter = Leaf(
                    target = Column("c2"),
                    constraint = Binary(Operator.NOT_EQUAL, Column("c3"))
                )
            )
        )
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when OR and AND expressions are used then they are correclty parsed`() {
        val raw = StringUtil.toSingleLine("""
            select t1.c1 T1, t2.c2
            from table1 t1, table2 t2
            where t1.c11 = 1
                  and t2.c21 != 2
                  and (
                    t1.c12 != 3
                    or
                    (t2.c22 = t1.c13 and t2.c14 = 4)
                  )
                  and t1.c21 = t2.c22
        """)
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(
            Select(
                columns = listOf(
                    SelectTarget(Column(name = "c1", table = "t1"), alias = "T1"),
                    SelectTarget(Column(name = "c2", table = "t2"), alias = null)
                ),
                table = Table(name = "table1", alias = "t1"),
                joins = listOf(Join(Table(name = "table2", alias = "t2"), null, JoinMethod.SIMPLE)),
                filter = And(
                    listOf(
                        Leaf(
                            target = Column(name = "c11", table = "t1"),
                            constraint = Binary(Operator.EQUAL, LongLiteral(1L))
                        ),
                        Leaf(
                            target = Column(name = "c21", table = "t2"),
                            constraint = Binary(Operator.NOT_EQUAL, LongLiteral(2L))
                        ),
                        Or(
                            listOf(
                                Leaf(
                                    target = Column(name = "c12", table = "t1"),
                                    constraint = Binary(Operator.NOT_EQUAL, LongLiteral(3L))
                                ),
                                And(
                                    listOf(
                                        Leaf(
                                            target = Column(name = "c22", table = "t2"),
                                            constraint = Binary(Operator.EQUAL, Column(name = "c13", table = "t1"))
                                        ),
                                        Leaf(
                                            target = Column(name = "c14", table = "t2"),
                                            constraint = Binary(Operator.EQUAL, LongLiteral(4L))
                                        )
                                    )
                                )
                            )
                        ),
                        Leaf(
                            target = Column(name = "c21", table = "t1"),
                            constraint = Binary(Operator.EQUAL, Column(name = "c22", table = "t2"))
                        )
                    )
                )
            )
        )
        assertThat(sql.sql).isEqualTo(StringUtil.toSingleLine("""
            select t1.c1 T1, t2.c2
            from table1 t1, table2 t2
            where (t1.c11 = 1
              and t2.c21 != 2
              and (t1.c12 != 3
                or (t2.c22 = t1.c13 and t2.c14 = 4))
              and t1.c21 = t2.c22)
        """))
    }

    @Test
    fun `when LIKE filter is used then it is correctly parsed`() {
        val raw = "select distinct c1, c2 from t1 where c3 like '%(B)%'"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(
            Select(
                columns = listOf(SelectTarget(Column("c1")), SelectTarget(Column("c2"))),
                table = Table("t1"),
                filter = Leaf(
                    target = Column("c3"),
                    constraint = Binary(Operator.LIKE, StringLiteral("%(B)%"))
                ),
                distinct = true
            )
        )
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when NOT LIKE filter is used then it is correctly parsed`() {
        val raw = "select distinct c1, c2 from t1 where c3 not like '%(B)%'"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(
            Select(
                columns = listOf(SelectTarget(Column("c1")), SelectTarget(Column("c2"))),
                table = Table("t1"),
                filter = Leaf(
                    target = Column("c3"),
                    constraint = Binary(Operator.NOT_LIKE, StringLiteral("%(B)%"))
                ),
                distinct = true
            )
        )
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when NULL filter is used then it is correctly parsed`() {
        val raw = "select c1, c2 from t1 where c1 is null and c2 is not null"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(
            Select(
                columns = listOf(SelectTarget(Column("c1")), SelectTarget(Column("c2"))),
                table = Table("t1"),
                filter = And(listOf(
                    Leaf(target = Column("c1"), constraint = IsNull),
                    Leaf(target = Column("c2"), constraint = IsNotNull)
                ))
            )
        )
        assertThat(sql.sql).isEqualTo("select c1, c2 from t1 where (c1 is null and c2 is not null)")
    }

    @Test
    fun `when IN filter is used then it is correctly parsed`() {
        val raw = "select c1 from t1 where c1 in (1, 2) and c2 in ('a', 'b') and c3 not in (3) and c4 not in ('c')"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(
            Select(
                columns = listOf(SelectTarget(Column("c1"))),
                table = Table("t1"),
                filter = And(listOf(
                    Leaf(target = Column("c1"), constraint = In(listOf(LongLiteral(1L), LongLiteral(2L)))),
                    Leaf(target = Column("c2"), constraint = In(listOf(StringLiteral("a"), StringLiteral("b")))),
                    Leaf(target = Column("c3"), constraint = NotIn(listOf(LongLiteral(3L)))),
                    Leaf(target = Column("c4"), constraint = NotIn(listOf(StringLiteral("c"))))
                ))
            )
        )
        assertThat(sql.sql).isEqualTo(
            "select c1 from t1 where (c1 in (1, 2) and c2 in ('a', 'b') and c3 not in (3) and c4 not in ('c'))"
        )
    }

    @Test
    fun `when functions are used then sql is correctly parsed`() {
        val raw = StringUtil.toSingleLine("""
            select sum(o.qty) total, round(sum(f.done * f.price))
            from orders o, fills f
            where
              o.orderId = f.orderId
              and (round(sum(f.done*f.price)/ sum(o.done)) > 10)
        """)
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(
                SelectTarget(
                    target = Function(name = "sum", operands = listOf(Column(name = "qty", table = "o"))),
                    alias = "total"
                ),
                SelectTarget(
                    target = Function(
                        name = "round", operands = listOf(
                            Function(
                                name = "sum",
                                operands = listOf(
                                    OperatorFunction(
                                        operator = "*",
                                        left = Column(name = "done", table = "f"),
                                        right = Column(name = "price", table = "f")
                                    )
                                )
                            )
                        )
                    ),
                    alias = null
                )
            ),
            table = Table(name = "orders", alias = "o"),
            joins = listOf(Join(Table(name = "fills", alias = "f"), null, JoinMethod.SIMPLE)),
            filter = And(listOf(
                Leaf(
                    target = Column(name = "orderId", table = "o"),
                    constraint = Binary(Operator.EQUAL, Column(name = "orderId", table = "f"))
                ),
                Leaf(
                    target = Function(
                        name = "round",
                        operands = listOf(
                            OperatorFunction(
                                operator = "/",
                                left = Function(
                                    name = "sum",
                                    operands = listOf(
                                        OperatorFunction(
                                            operator = "*",
                                            left = Column(name = "done", table = "f"),
                                            right = Column(name = "price", table = "f")
                                        )
                                    )
                                ),
                                right = Function(
                                    name = "sum",
                                    operands = listOf(Column(name = "done", table = "o"))
                                )
                            )
                        )
                    ),
                    constraint = Binary(Operator.GREATER, LongLiteral(10L))
                )
            ))
        ))
        assertThat(sql.sql).isEqualTo(StringUtil.toSingleLine("""
            select sum(o.qty) total, round(sum(f.done * f.price))
            from orders o, fills f
            where (o.orderId = f.orderId
              and round(sum(f.done * f.price) / sum(o.done)) > 10)
        """))
    }

    @Test
    fun `when GREATER_OR_EQUAL constraint is used then it is correctly parsed`() {
        val raw = "select a from t where b >= 10"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("a"))),
            table = Table("t"),
            filter = Leaf(
                target = Column("b"),
                constraint = Binary(Operator.GREATER_OR_EQUAL, LongLiteral(10L))
            )
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when LESS constraint is used then it is correctly parsed`() {
        val raw = "select a from t where b < 10"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("a"))),
            table = Table("t"),
            filter = Leaf(
                target = Column("b"),
                constraint = Binary(Operator.LESS, LongLiteral(10L))
            )
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when LESS_OR_EQUAL constraint is used then it is correctly parsed`() {
        val raw = "select a from t where b <= 10"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("a"))),
            table = Table("t"),
            filter = Leaf(
                target = Column("b"),
                constraint = Binary(Operator.LESS_OR_EQUAL, LongLiteral(10L))
            )
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when sybase style rows number limit is set then it is correctly parsed`() {
        val raw = "select top 10 c1 from t where c2 > 3"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("c1"))),
            table = Table("t"),
            filter = Leaf(
                target = Column("c2"),
                constraint = Binary(Operator.GREATER, LongLiteral(3L))
            ),
            top = 10
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when BETWEEN constraint is used then it is correctly parsed`() {
        val raw = "select c1 from t where c2 between 1 and 101"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("c1"))),
            table = Table("t"),
            filter = Leaf(
                target = Column("c2"),
                constraint = Between(LongLiteral(1L), LongLiteral(101L))
            )
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when top level filter is negated then it is correctly parsed`() {
        val raw = "select c1 from t where not (c2 = 'v1' and c3 like 'v2%')"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("c1"))),
            table = Table("t"),
            filter = Not(
                And(listOf(
                    Leaf(target = Column("c2"), constraint = Binary(Operator.EQUAL, StringLiteral("v1"))),
                    Leaf(target = Column("c3"), constraint = Binary(Operator.LIKE, StringLiteral("v2%")))
                ))
            )
        ))
        assertThat(sql.sql).isEqualTo("select c1 from t where not ((c2 = 'v1' and c3 like 'v2%'))")
    }

    @Test
    fun `when nested filter is negated then it is correctly parsed`() {
        val raw = "select c1 from t where c2 = 'v1' and not (c3 like 'v2%')"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("c1"))),
            table = Table("t"),
            filter = And(listOf(
                Leaf(target = Column("c2"), constraint = Binary(Operator.EQUAL, StringLiteral("v1"))),
                Not(Leaf(target = Column("c3"), constraint = Binary(Operator.LIKE, StringLiteral("v2%"))))
            ))
        ))
        assertThat(sql.sql).isEqualTo("select c1 from t where (c2 = 'v1' and not (c3 like 'v2%'))")
    }

    @Test
    fun `when select targets all columns then it is correctly parsed`() {
        val raw = "select * from t where c1 = 'v1'"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(AllColumns)),
            table = Table("t"),
            filter = Leaf(target = Column("c1"), constraint = Binary(Operator.EQUAL, StringLiteral("v1")))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when select uses ORDER BY then it is correctly parsed`() {
        val raw = "select a, b from c order by b asc, a desc"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("a")), SelectTarget(Column("b"))),
            table = Table("c"),
            orderBy = listOf(
                OrderBy(Column("b"), true),
                OrderBy(Column("a"), false)
            )
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when select uses a filter and ORDER BY then it is correctly parsed`() {
        val raw = "select a, b from c where d = 1 order by b asc, a desc"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("a")), SelectTarget(Column("b"))),
            table = Table("c"),
            filter = Leaf(target = Column("d"), constraint = Binary(Operator.EQUAL, LongLiteral(1L))),
            orderBy = listOf(
                OrderBy(Column("b"), true),
                OrderBy(Column("a"), false)
            )
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when select uses GROUP BY then it is correctly parsed`() {
        val raw = "select a, sum(b), d from c group by a, d"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(
                SelectTarget(Column("a")),
                SelectTarget(Function("sum", listOf(Column("b")))),
                SelectTarget(Column("d"))
            ),
            table = Table("c"),
            groupBy = listOf(Column("a"), Column("d"))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when literals and addition and subtraction are used then it is correctly parsed`() {
        val raw = StringUtil.toSingleLine("""
            select
              substring(a, 1, patindex('%.%', a) - 2)
              + '.'
              + substring(a, patindex('%.%', a) + 1, len(a)) new_a, a
            from t
            where (b = 'S' and a like '%n.B')
        """)
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(
                SelectTarget(
                    target = OperatorFunction(
                        operator = "+",
                        left = OperatorFunction(
                            operator = "+",
                            left = Function(
                                name = "substring",
                                operands = listOf(
                                    Column("a"),
                                    LongLiteral(1L),
                                    OperatorFunction(
                                        operator = "-",
                                        left = Function(
                                            name = "patindex",
                                            operands = listOf(
                                                StringLiteral("%.%"),
                                                Column("a")
                                            )
                                        ),
                                        right = LongLiteral(2L)
                                    )
                                )
                            ),
                            right = StringLiteral(".")
                        ),
                        right = Function(
                            name = "substring",
                            operands = listOf(
                                Column("a"),
                                OperatorFunction(
                                    operator = "+",
                                    left = Function(
                                        name = "patindex",
                                        operands = listOf(
                                            StringLiteral("%.%"),
                                            Column("a")
                                        )
                                    ),
                                    right = LongLiteral(1L)
                                ),
                                Function(
                                    name = "len",
                                    operands = listOf(Column("a"))
                                )
                            )
                        )
                    ),
                    alias = "new_a"
                ),
                SelectTarget(Column("a"))
            ),
            table = Table("t"),
            filter = And(listOf(
                Leaf(Column("b"), Binary(Operator.EQUAL, StringLiteral("S"))),
                Leaf(Column("a"), Binary(Operator.LIKE, StringLiteral("%n.B")))
            ))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when negative number is used the it is processed correctly`() {
        val raw = "select c1, -1, -1.1 from t where (c2 = -2 and c3 > -3.3 and c4 < -4 and c5 != -5.5)"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(
                SelectTarget(Column("c1")),
                SelectTarget(LongLiteral(-1L)),
                SelectTarget(DoubleLiteral(-1.1))
            ),
            table = Table("t"),
            filter = And(listOf(
                Leaf(Column("c2"), Binary(Operator.EQUAL, LongLiteral(-2L))),
                Leaf(Column("c3"), Binary(Operator.GREATER, DoubleLiteral(-3.3))),
                Leaf(Column("c4"), Binary(Operator.LESS, LongLiteral(-4L))),
                Leaf(Column("c5"), Binary(Operator.NOT_EQUAL, DoubleLiteral(-5.5))),
            ))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when select uses jdbc parameter then it is processed correctly`() {
        val raw = "select c1 from t where (c1 = ? and c2 = ?)"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("c1"))),
            table = Table("t"),
            filter = And(listOf(
                Leaf(Column("c1"), Binary(Operator.EQUAL, Placeholder("?"))),
                Leaf(Column("c2"), Binary(Operator.EQUAL, Placeholder("?")))
            ))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when select uses simple GROUP BY and HAVING then it is processed correctly`() {
        val raw = "select c1, c2 from t where c1 like '%-S' group by c2 having count(*) > 1"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("c1")), SelectTarget(Column("c2"))),
            table = Table("t"),
            filter = Leaf(Column("c1"), Binary(Operator.LIKE, StringLiteral("%-S"))),
            groupBy = listOf(Column("c2")),
            having = Leaf(Function("count", listOf(AllColumns)), Binary(Operator.GREATER, LongLiteral(1L)))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when select uses composite HAVING then it is processed correctly`() {
        val raw = "select c1, c2 from t group by c2 having (count(*) > 1 and c1 like '%-S')"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("c1")), SelectTarget(Column("c2"))),
            table = Table("t"),
            groupBy = listOf(Column("c2")),
            having = And(listOf(
                Leaf(Function("count", listOf(AllColumns)), Binary(Operator.GREATER, LongLiteral(1L))),
                Leaf(Column("c1"), Binary(Operator.LIKE, StringLiteral("%-S")))
            ))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when filter has SUB-SELECT then it is processed correctly`() {
        val raw = StringUtil.toSingleLine("""
            select max(c1) c1
            from t
            where (c2 = 'S'
              and c1 < (select max(c1) c1
                        from t
                        where (c2 like '%.SI%' and c3 = 'S'
                          and (c4 is null or c4 = ''))))
        """)
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Function("max", listOf(Column("c1"))), "c1")),
            table = Table("t"),
            filter = And(listOf(
                Leaf(Column("c2"), Binary(Operator.EQUAL, StringLiteral("S"))),
                Leaf(Column("c1"), Binary(
                    Operator.LESS, SubSelect(
                        select = Select(
                            columns = listOf(SelectTarget(Function("max", listOf(Column("c1"))), "c1")),
                            table = Table("t"),
                            filter = And(listOf(
                                Leaf(Column("c2"), Binary(Operator.LIKE, StringLiteral("%.SI%"))),
                                Leaf(Column("c3"), Binary(Operator.EQUAL, StringLiteral("S"))),
                                Or(listOf(
                                    Leaf(Column("c4"), IsNull),
                                    Leaf(Column("c4"), Binary(Operator.EQUAL, StringLiteral("")))
                                ))
                            ))
                        ),
                        withParentheses = true
                    )
                ))
            ))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when SUB-SELECT is used as column then it is processed correctly`() {
        val raw = StringUtil.toSingleLine("""
            select rtrim(f.d) d,
                   (select max(acc) from o where (s = 'A' and r = o.r)) P
            from orders o, fill f
        """)
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(
                SelectTarget(Function("rtrim", listOf(Column("d", "f"))), "d"),
                SelectTarget(
                    target = SubSelect(
                        select = Select(
                            columns = listOf(SelectTarget(Function("max", listOf(Column("acc"))))),
                            table = Table("o"),
                            filter = And(listOf(
                                Leaf(Column("s"), Binary(Operator.EQUAL, StringLiteral("A"))),
                                Leaf(Column("r"), Binary(Operator.EQUAL, Column("r", "o")))
                            ))
                        ),
                        withParentheses = true
                    ),
                    alias = "P"
                )
            ),
            table = Table("orders", "o"),
            joins = listOf(Join(Table("fill", "f"), null, JoinMethod.SIMPLE))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when INNER JOIN is used then it is processed correctly`() {
        val raw = StringUtil.toSingleLine("""
            select f.s s
            from os o inner join fs f on (f.o = o.o
              and o.s like '%.SI')
            where o.p is null
            order by s asc
        """)
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("s", "f"), "s")),
            table = Table("os", "o"),
            joins = listOf(Join(
                table = Table("fs", "f"),
                on = And(listOf(
                    Leaf(Column("o", "f"), Binary(Operator.EQUAL, Column("o", "o"))),
                    Leaf(Column("s", "o"), Binary(Operator.LIKE, StringLiteral("%.SI")))
                )),
                method = JoinMethod.INNER
            )),
            filter = Leaf(Column("p", "o"), IsNull),
            orderBy = listOf(OrderBy(Column("s"), true))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when UNION is used then it is processed correctly`() {
        val raw = StringUtil.toSingleLine("""
            select f.s s
            from os o, fs f
            where o.a = f.a
            UNION
            select f.s s
            from os o inner join fs f on (f.o = o.o and o.s like '%.SI')
        """)
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            listOf(SelectTarget(Column("s", "f"), "s")),
            table = Table("os", "o"),
            joins = listOf(Join(Table("fs", "f"), null, JoinMethod.SIMPLE)),
            filter = Leaf(Column("a", "o"), Binary(Operator.EQUAL, Column("a", "f"))),
            operations = listOf(Operation(
                type = "UNION",
                select = Select(
                    columns = listOf(SelectTarget(Column("s", "f"), "s")),
                    table = Table("os", "o"),
                    joins = listOf(Join(
                        table = Table("fs", "f"),
                        on = And(listOf(
                            Leaf(Column("o", "f"), Binary(Operator.EQUAL, Column("o", "o"))),
                            Leaf(Column("s", "o"), Binary(Operator.LIKE, StringLiteral("%.SI")))
                        )),
                        method = JoinMethod.INNER
                    ))
                )
            ))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when operator function is used as select target then it is processed correctly`() {
        val raw = StringUtil.toSingleLine("""
            select distinct o.g o, f.g gf, (f.q + f.fq) c
            from os o, fs f
            where (f.g = o.g and f.s != 'c')
        """)
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(
                SelectTarget(Column("g", "o"), "o"),
                SelectTarget(Column("g", "f"), "gf"),
                SelectTarget(
                    target = OperatorFunction(
                        operator = "+",
                        left = Column("q", "f"),
                        right = Column("fq", "f"),
                        withParentheses = true
                    ),
                    alias = "c"
                )
            ),
            distinct = true,
            table = Table("os", "o"),
            joins = listOf(Join(Table("fs", "f"), null, JoinMethod.SIMPLE)),
            filter = And(listOf(
                Leaf(Column("g", "f"), Binary(Operator.EQUAL, Column("g", "o"))),
                Leaf(Column("s", "f"), Binary(Operator.NOT_EQUAL, StringLiteral("c")))
            ))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when oracle date conversion is used then it is processed correctly`() {
        val raw = StringUtil.toSingleLine("""
            select c1
            from t
            where (r >= DATE '2022-02-16'
              and u not in ('x'))
        """)
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("c1"))),
            table = Table("t"),
            filter = And(listOf(
                Leaf(
                    target = Column("r"),
                    constraint = Binary(
                        operator = Operator.GREATER_OR_EQUAL,
                        target = DateTimeLiteral("'2022-02-16'", DateTimeLiteralExpression.DateTime.DATE)
                    )
                ),
                Leaf(Column("u"), NotIn(listOf(StringLiteral("x"))))
            ))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }

    @Test
    fun `when field equality check is used then it is processed correctly`() {
        val raw = "select a from t where a = b"
        val sql = parser.parse(raw)
        assertThat(sql).isEqualTo(Select(
            columns = listOf(SelectTarget(Column("a"))),
            table = Table("t"),
            filter = Leaf(Column("a"), Binary(Operator.EQUAL, Column("b")))
        ))
        assertThat(sql.sql).isEqualTo(raw)
    }
}