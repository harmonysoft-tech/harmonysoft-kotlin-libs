package tech.harmonysoft.oss.sql.dsl.join

import net.sf.jsqlparser.statement.select.Join

enum class JoinMethod {
    OUTER, RIGHT, LEFT, FULL, INNER, CROSS,

    /**
     * We can just simply query columns without `join` or `on` keywords.
     * For example, `select o.OrderId, i.Price from orders o, prices p`.
     * We treat this as [SIMPLE], following [Join.isSimple]
     */
    SIMPLE
}