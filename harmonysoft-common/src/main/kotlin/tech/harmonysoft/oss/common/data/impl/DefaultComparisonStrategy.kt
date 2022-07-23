package tech.harmonysoft.oss.common.data.impl

import tech.harmonysoft.oss.common.data.ComparisonStrategy
import tech.harmonysoft.oss.common.type.TypeManagerContext
import tech.harmonysoft.oss.common.type.TypeManagersHelper
import kotlin.reflect.KClass

class DefaultComparisonStrategy(
    private val typeManagersHelper: TypeManagersHelper,
    private val contexts: Set<TypeManagerContext>
) : ComparisonStrategy {

    override fun <T : Any> compare(targetType: KClass<T>, first: T?, second: T?): Int? {
        return when {
            first == null && second == null -> 0
            first != null -> typeManagersHelper.getTypeManager(targetType, contexts).compareTo(first, second)
            second != null -> ComparisonStrategy.inverse(
                typeManagersHelper.getTypeManager(targetType, contexts).compareTo(second, first)
            )
            else -> throw UnsupportedOperationException("I can't happen")
        }
    }
}