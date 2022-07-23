package tech.harmonysoft.oss.sql.dsl.filter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget
import tech.harmonysoft.oss.sql.parser.SqlParser

internal class FilterDslTest {

    private val parser = SqlParser()

    @Test
    fun `when leaf is removed from itself then it works as expected`() {
        val initialFilter = parser.parseFilter("a != 2")
        val resultingFilter = initialFilter - initialFilter
        assertThat(resultingFilter).isNull()
    }

    @Test
    fun `when other filter is removed from leaf filter then it works as expected`() {
        val initialFilter = parser.parseFilter("a != 2")
        val resultingFilter = initialFilter - parser.parseFilter("a != 2")
        assertThat(resultingFilter).isEqualTo(initialFilter)
    }

    @Test
    fun `when top-level AND is removed then it works as expected`() {
        val initialFilter = parser.parseFilter("a != 2 and b = 2")
        val resultingFilter = initialFilter - initialFilter
        assertThat(resultingFilter).isNull()
    }

    @Test
    fun `when top-level child is removed from AND then it works as expected`() {
        val initialFilter = parser.parseFilter("a != 2 and b = 2")
        val resultingFilter = initialFilter - (initialFilter as Filter.And).filters.first()
        assertThat(resultingFilter.toString()).isEqualTo("b = 2")
    }

    @Test
    fun `when not top-level child is removed from AND then it works as expected`() {
        val initialFilter = parser.parseFilter("a != 2 and (b = 2 and c in (3, 4))")
        val toRemove = ((initialFilter as Filter.And).filters.last() as Filter.And).filters.first()
        val resultingFilter = initialFilter - toRemove
        assertThat(resultingFilter.toString()).isEqualTo("(a != 2 and c in (3, 4))")
    }

    @Test
    fun `when sub-filter is removed from AND then identity is preserved`() {
        val initialFilter = parser.parseFilter("a != 2 and (b = 2 and c in (3, 4))")
        val toRemove = ((initialFilter as Filter.And).filters.last() as Filter.And).filters.first() as Filter.Leaf
        val resultingFilter = initialFilter - toRemove.copy()
        assertThat(resultingFilter).isEqualTo(initialFilter)
    }

    @Test
    fun `when top-level OR is removed then it works as expected`() {
        val initialFilter = parser.parseFilter("a != 2 or b = 2")
        val resultingFilter = initialFilter - initialFilter
        assertThat(resultingFilter).isNull()
    }

    @Test
    fun `when top-level child is removed from OR then it works as expected`() {
        val initialFilter = parser.parseFilter("a != 2 or b = 2")
        val resultingFilter = initialFilter - (initialFilter as Filter.Or).filters.last()
        assertThat(resultingFilter.toString()).isEqualTo("a != 2")
    }

    @Test
    fun `when not top-level child is removed from OR then it works as expected`() {
        val initialFilter = parser.parseFilter("a != 2 or (b = 2 or c in (3, 4))")
        val toRemove = ((initialFilter as Filter.Or).filters.last() as Filter.Or).filters.last()
        val resultingFilter = initialFilter - toRemove
        assertThat(resultingFilter.toString()).isEqualTo("(a != 2 or b = 2)")
    }

    @Test
    fun `when sub-filter is removed from OR then identity is preserved`() {
        val initialFilter = parser.parseFilter("a != 2 or (b = 2 or c in (3, 4))")
        val toRemove = ((initialFilter as Filter.Or).filters.last() as Filter.Or).filters.last() as Filter.Leaf
        val resultingFilter = initialFilter - toRemove.copy()
        assertThat(resultingFilter).isEqualTo(initialFilter)
    }

    @Test
    fun `when complete filter is removed from NOT then it works as expected`() {
        val initialFilter = parser.parseFilter("not (a != 2 or b = 3)")
        assertThat(initialFilter - initialFilter).isNull()
    }

    @Test
    fun `when complete wrapped filter is removed from NOT then it works as expected`() {
        val initialFilter = parser.parseFilter("not (a != 2 or b = 3)")
        val toRemove = (initialFilter as Filter.Not).filter
        val resultingFilter = initialFilter - toRemove
        assertThat(resultingFilter).isNull()
    }

    @Test
    fun `when sub-filter is removed from NOT then it works as expected`() {
        val initialFilter = parser.parseFilter("not (a != 2 or b = 3)")
        val toRemove = ((initialFilter as Filter.Not).filter as Filter.Or).filters.last()
        val resultingFilter = initialFilter - toRemove
        assertThat(resultingFilter?.toString()).isEqualTo("not (a != 2)")
    }

    @Test
    fun `when only target columns are asked to be kept in filter then it works as expected`() {
        val initialFilter = parser.parseFilter("(a = 1 and b = 2) or (a = 2 and c = 3)")
        val resultingFilter = initialFilter.keepColumns(setOf("b", "c"))
        assertThat(resultingFilter?.toString()).isEqualTo("(b = 2 or c = 3)")
    }

    @Test
    fun `when target columns are dropped from filter then it works as expected`() {
        val initialFilter = parser.parseFilter("a = 1 or (a = 2 and b = 3)")
        val resultingFilter = initialFilter.dropColumns(setOf("a"))
        assertThat(resultingFilter?.toString()).isEqualTo("b = 3")
    }

    @Test
    fun `when a value is removed from IN filter then it works as expected`() {
        val initialFilter = parser.parseFilter("a = 1 or b in (1, 2, 3)")
        val resultingFilter = initialFilter.removeValue("b", SqlTarget.LongLiteral(2))
        assertThat(resultingFilter?.toString()).isEqualTo("(a = 1 or b in (1, 3))")
    }
}