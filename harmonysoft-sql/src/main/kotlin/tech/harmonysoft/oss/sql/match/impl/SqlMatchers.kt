package tech.harmonysoft.oss.sql.match.impl

import tech.harmonysoft.oss.common.data.ComparisonStrategy
import tech.harmonysoft.oss.common.data.DataProviderStrategy
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget
import tech.harmonysoft.oss.sql.match.SqlKeyValueMatcher
import tech.harmonysoft.oss.sql.match.SqlKeyValueVisitor
import java.util.regex.Pattern
import kotlin.reflect.KClass

object MatchNoneMatcher : SqlKeyValueMatcher {

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        return false
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }

    override fun toString(): String {
        return "<match none>"
    }
}

object MatchAllMatcher : SqlKeyValueMatcher {

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        return true
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }

    override fun toString(): String {
        return "<match all>"
    }
}

interface SqlKeyValueLeafMatcher : SqlKeyValueMatcher {
    val key: SqlTarget.Column
}

abstract class AbstractLeafMatcher(
    override val key: SqlTarget.Column,
    val value: Any,
    val comparison: ComparisonStrategy,
    _valueType: KClass<*>
) : SqlKeyValueLeafMatcher {

    @Suppress("UNCHECKED_CAST")
    val valueType: KClass<Any> = _valueType as KClass<Any>

    fun <HOLDER> retrieveComparedValue(
        retrievalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>,
        holder: HOLDER
    ) = if (value is SqlTarget.Column) {
        retrievalStrategy.getData(holder, value)
    } else {
        value
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + comparison.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        return (other as? AbstractLeafMatcher)?.let {
            key == it.key && comparison.compare(valueType, value, it.value) == 0
        } ?: false
    }

    override fun toString(): String {
        val valueString = if (value is String) {
            "'$value'"
        } else {
            "$value"
        }
        return "$key ${this::class.simpleName} $valueString"
    }
}

data class Not(
    val delegate: SqlKeyValueMatcher
) : SqlKeyValueMatcher {

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        return !delegate.matches(holder, retrivalStrategy)
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }

    override fun toString(): String {
        return "not ($delegate"
    }
}

class Eq(
    key: SqlTarget.Column,
    valueType: KClass<*>,
    value: Any,
    comparison: ComparisonStrategy
) : AbstractLeafMatcher(key, value, comparison, valueType) {

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        val actualValue = retrivalStrategy.getData(holder, key)
        val comparedValue = retrieveComparedValue(retrivalStrategy, holder)
        return comparison.compare(valueType, comparedValue, actualValue) == 0
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }
}

class Gt(
    key: SqlTarget.Column,
    valueType: KClass<*>,
    value: Any,
    comparison: ComparisonStrategy
) : AbstractLeafMatcher(key, value, comparison, valueType) {

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        val comparedValue = retrieveComparedValue(retrivalStrategy, holder)
        val cmp = comparison.compare(valueType, retrivalStrategy.getData(holder, key), comparedValue) ?: return false
        return cmp > 0
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }
}

class Ge(
    key: SqlTarget.Column,
    valueType: KClass<*>,
    value: Any,
    comparison: ComparisonStrategy
) : AbstractLeafMatcher(key, value, comparison, valueType) {

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        val comparedValue = retrieveComparedValue(retrivalStrategy, holder)
        val cmp = comparison.compare(valueType, retrivalStrategy.getData(holder, key), comparedValue) ?: return false
        return cmp >= 0
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }
}

class Lt(
    key: SqlTarget.Column,
    valueType: KClass<*>,
    value: Any,
    comparison: ComparisonStrategy
) : AbstractLeafMatcher(key, value, comparison, valueType) {

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        val comparedValue = retrieveComparedValue(retrivalStrategy, holder)
        val cmp = comparison.compare(valueType, retrivalStrategy.getData(holder, key), comparedValue) ?: return false
        return cmp <0
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }
}

class Le(
    key: SqlTarget.Column,
    valueType: KClass<*>,
    value: Any,
    comparison: ComparisonStrategy
) : AbstractLeafMatcher(key, value, comparison, valueType) {

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        val comparedValue = retrieveComparedValue(retrivalStrategy, holder)
        val cmp = comparison.compare(valueType, retrivalStrategy.getData(holder, key), comparedValue) ?: return false
        return cmp <= 0
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }
}

data class Like(
    override val key: SqlTarget.Column,
    val value: String
) : SqlKeyValueLeafMatcher {

    private val pattern = Pattern.compile(escapeSpecialRegexSymbols(value).replace("%", ".*"))

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        val actualValue = retrivalStrategy.getData(holder, key)?.toString()
        return actualValue != null && pattern.matcher(actualValue).matches()
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }

    override fun toString(): String {
        return "$key LIKE '$value'"
    }

    companion object {

        val SPECIAL_SYMBOLS = setOf(
            "\\", ".", "[", "]", "{", "}", "(", ")", "<", ">", "*", "+", "-", "=", "?", "^", "$", "|"
        ).associateWith { "\\$it" }

        fun escapeSpecialRegexSymbols(s: String): String {
            return SPECIAL_SYMBOLS.entries.fold(s) { current, entry ->
                current.replace(entry.key, entry.value)
            }
        }
    }
}

data class In(
    override val key: SqlTarget.Column,
    val valueType: KClass<*>,
    val values: Set<Any>,
    val comparison: ComparisonStrategy
) : SqlKeyValueLeafMatcher {

    @Suppress("UNCHECKED_CAST")
    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        val actualValue = retrivalStrategy.getData(holder, key)
        return values.any {
            comparison.compare(valueType as KClass<Any>, actualValue, it) == 0
        }
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }

    override fun toString(): String {
        return "$key IN (${values.joinToString()})"
    }
}

data class Between(
    override val key: SqlTarget.Column,
    val valueType: KClass<*>,
    val startValue: Any,
    val endValue: Any,
    val comparison: ComparisonStrategy
) : SqlKeyValueLeafMatcher {

    @Suppress("UNCHECKED_CAST")
    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        val actualValue = retrivalStrategy.getData(holder, key) ?: return false

        val startCmp = comparison.compare(valueType as KClass<Any>, actualValue, startValue)
        if (startCmp == null || startCmp < 0) {
            return false
        }

        val endCmp = comparison.compare(valueType, actualValue, endValue)
        return endCmp != null && endCmp <= 0
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }

    override fun toString(): String {
        return "$key BETWEEN $startValue AND $endValue"
    }
}

data class IsNull(
    override val key: SqlTarget.Column
) : SqlKeyValueLeafMatcher {

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        val actualValue = retrivalStrategy.getData(holder, key)
        return actualValue == null
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }

    override fun toString(): String {
        return "$key IS NULL"
    }
}

data class Or(
    val _matchers: Collection<SqlKeyValueMatcher>
) : SqlKeyValueMatcher {

    private val matchers = _matchers.toTypedArray()

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        return matchers.any {
            it.matches(holder, retrivalStrategy)
        }
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }

    override fun toString(): String {
        return matchers.joinToString(prefix = "(", separator = ") or (", postfix = ")")
    }
}

data class And(
    val _matchers: Collection<SqlKeyValueMatcher>
) : SqlKeyValueMatcher {

    private val matchers = _matchers.toTypedArray()

    override fun <HOLDER> matches(
        holder: HOLDER,
        retrivalStrategy: DataProviderStrategy<HOLDER, SqlTarget.Column>
    ): Boolean {
        return matchers.all {
            it.matches(holder, retrivalStrategy)
        }
    }

    override fun accept(visitor: SqlKeyValueVisitor) {
        visitor.visit(this)
    }

    override fun toString(): String {
        return matchers.joinToString(prefix = "(", separator = ") and (", postfix = ")")
    }
}