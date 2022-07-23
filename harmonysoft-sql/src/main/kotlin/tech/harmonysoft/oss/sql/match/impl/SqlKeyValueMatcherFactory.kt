package tech.harmonysoft.oss.sql.match.impl

import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.data.ComparisonStrategy
import tech.harmonysoft.oss.common.data.TypedKeyManager
import tech.harmonysoft.oss.common.data.impl.DefaultComparisonStrategyFactory
import tech.harmonysoft.oss.common.match.KeyValueMatcher
import tech.harmonysoft.oss.common.match.KeyValueMatcherFactory
import tech.harmonysoft.oss.common.type.TypeManagerContext
import tech.harmonysoft.oss.common.type.TypeManagersHelper
import tech.harmonysoft.oss.sql.dsl.constraint.Constraint
import tech.harmonysoft.oss.sql.dsl.constraint.Operator
import tech.harmonysoft.oss.sql.dsl.filter.Filter
import tech.harmonysoft.oss.sql.dsl.target.SqlTarget
import tech.harmonysoft.oss.sql.match.SqlKeyValueMatcher
import tech.harmonysoft.oss.sql.parser.SqlParser
import javax.inject.Named

@Named
class SqlKeyValueMatcherFactory(
    private val parser: SqlParser,
    private val typeManagerHelper: TypeManagersHelper,
    private val defaultComparisonStrategyFactory: DefaultComparisonStrategyFactory
) : KeyValueMatcherFactory<SqlTarget.Column> {

    private val logger = LoggerFactory.getLogger(SqlKeyValueMatcherFactory::class.java)

    override fun build(
        rule: String,
        keyManager: TypedKeyManager<SqlTarget.Column>,
        vararg contexts: TypeManagerContext
    ): KeyValueMatcher<SqlTarget.Column> {
        return build(
            rule = rule,
            keyManager = keyManager,
            comparison = defaultComparisonStrategyFactory.getStrategy(contexts.toSet()),
            contexts = contexts
        )
    }

    override fun build(
        rule: String,
        keyManager: TypedKeyManager<SqlTarget.Column>,
        comparison: ComparisonStrategy,
        vararg contexts: TypeManagerContext
    ): KeyValueMatcher<SqlTarget.Column> {
        return try {
            build(parser.parseFilter(rule), MatcherBuildContext(keyManager, comparison, contexts.toSet()))
        } catch (e: Throwable) {
            logger.warn("Failed to build {} from '{}'", SqlKeyValueMatcher::class.simpleName, rule, e)
            throw e
        }
    }

    private fun build(filter: Filter, context: MatcherBuildContext): SqlKeyValueMatcher {
        return when (filter) {
            is Filter.And -> And(filter.filters.map { build(it, context) })
            is Filter.Or -> Or(filter.filters.map { build(it, context) })
            is Filter.Not -> Not(build(filter.filter, context))
            is Filter.Leaf -> buildFromLeafFilter(filter, context)
        }
    }

    private fun buildFromLeafFilter(filter: Filter.Leaf, context: MatcherBuildContext): SqlKeyValueMatcher {
        return when (filter.constraint) {
            is Constraint.Binary ->
                buildFromBinaryConstraint(filter.target as SqlTarget.Column, filter.constraint, context)
            is Constraint.In ->
                buildFromInConstraint(filter.target as SqlTarget.Column, filter.constraint.targetValues, context)
            is Constraint.NotIn ->
                Not(buildFromInConstraint(filter.target as SqlTarget.Column, filter.constraint.targetValues, context))
            is Constraint.Between ->
                buildFromBetweenConstraint(filter.target as SqlTarget.Column, filter.constraint, context)
            is Constraint.IsNull -> IsNull(filter.target as SqlTarget.Column)
            is Constraint.IsNotNull -> Not(IsNull(filter.target as SqlTarget.Column))
        }
    }

    private fun buildFromBinaryConstraint(
        key: SqlTarget.Column,
        constraint: Constraint.Binary,
        context: MatcherBuildContext
    ): SqlKeyValueMatcher {
        val valueType = context.keyManager.getValueType(key)
        val valueTypeManager = typeManagerHelper.getTypeManager(valueType, context.typeContexts)
        val value = if (constraint.target is SqlTarget.Column) {
            constraint.target
        } else {
            valueTypeManager.maybeParse(constraint.target.toString()) ?: throw IllegalArgumentException(
                "failed to parse a value of type ${valueType::qualifiedName} from '${constraint.target}' " +
                "via ${valueTypeManager::class.qualifiedName}"
            )
        }
        return when (constraint.operator) {
            Operator.EQUAL -> Eq(key, valueType, value, context.comparison)
            Operator.NOT_EQUAL -> Not(Eq(key, valueType, value, context.comparison))
            Operator.GREATER -> Gt(key, valueType, value, context.comparison)
            Operator.GREATER_OR_EQUAL -> Ge(key, valueType, value, context.comparison)
            Operator.LESS -> Lt(key, valueType, value, context.comparison)
            Operator.LESS_OR_EQUAL -> Le(key, valueType, value, context.comparison)
            Operator.LIKE -> Like(key, value.toString())
            Operator.NOT_LIKE -> Not(Like(key, value.toString()))
        }
    }

    private fun buildFromInConstraint(
        key: SqlTarget.Column,
        rawValues: Collection<Any>,
        context: MatcherBuildContext
    ): SqlKeyValueMatcher {
        val valueType = context.keyManager.getValueType(key)
        val valueTypeManager = typeManagerHelper.getTypeManager(valueType, context.typeContexts)
        val typedValues = rawValues.map {
            valueTypeManager.maybeParse(it.toString()) ?: throw IllegalArgumentException(
                "failed to parse a value of type ${valueType::qualifiedName} from '$it' " +
                "via ${valueTypeManager::class.qualifiedName}"
            )
        }
        return In(key, valueType, typedValues.toSet(), context.comparison)
    }

    private fun buildFromBetweenConstraint(
        key: SqlTarget.Column,
        constraint: Constraint.Between,
        context: MatcherBuildContext
    ): SqlKeyValueMatcher {
        val valueType = context.keyManager.getValueType(key)
        val valueTypeManager = typeManagerHelper.getTypeManager(valueType, context.typeContexts)
        return Between(
            key = key,
            valueType = valueType,
            startValue = valueTypeManager.maybeParse(constraint.min.toString()) ?: throw IllegalArgumentException(
                "failed to parse a value of type ${valueType::qualifiedName} from '${constraint.min}' " +
                "via ${valueTypeManager::class.qualifiedName}"
            ),
            endValue = valueTypeManager.maybeParse(constraint.max.toString()) ?: throw IllegalArgumentException(
                "failed to parse a value of type ${valueType::qualifiedName} from '${constraint.max}' " +
                "via ${valueTypeManager::class.qualifiedName}"
            ),
            comparison = context.comparison
        )
    }

    private data class MatcherBuildContext(
        val keyManager: TypedKeyManager<SqlTarget.Column>,
        val comparison: ComparisonStrategy,
        val typeContexts: Set<TypeManagerContext>
    )
}