package tech.harmonysoft.oss.common.data.impl

import tech.harmonysoft.oss.common.data.ComparisonStrategy
import tech.harmonysoft.oss.common.type.TypeManagerContext
import tech.harmonysoft.oss.common.type.TypeManagersHelper
import javax.inject.Named

@Named
class DefaultComparisonStrategyFactory(
    private val typeManagersHelper: TypeManagersHelper
) {

    fun getStrategy(contexts: Set<TypeManagerContext>): ComparisonStrategy {
        return DefaultComparisonStrategy(typeManagersHelper, contexts)
    }
}