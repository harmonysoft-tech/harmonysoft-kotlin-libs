package tech.harmonysoft.oss.common.template.factory.impl.provider

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext

data class StaticValueProvider<K>(
    val value: Any?
) : ValueProvider<K> {

    override val keys = emptySet<K>()

    override fun provide(context: KeyValueConfigurationContext<K>): Any? {
        return value
    }

    override fun toString(): String {
        return value?.toString() ?: "<null>"
    }
}