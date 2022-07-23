package tech.harmonysoft.oss.common.match

import tech.harmonysoft.oss.common.data.DataProviderStrategy

/**
 * A generic predicate for objects which can be represented via [DataProviderStrategy]
 */
interface KeyValueMatcher<KEY> {

    fun <HOLDER> matches(holder: HOLDER, retrivalStrategy: DataProviderStrategy<HOLDER, KEY>): Boolean
}