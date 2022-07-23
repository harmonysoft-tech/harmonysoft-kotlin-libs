package tech.harmonysoft.oss.common.template.factory.impl.provider

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext

data class DynamicKeyValueProvider<K>(
    val key: String
) : ValueProvider<K> {

    override val keys = emptySet<K>()

    override fun provide(context: KeyValueConfigurationContext<K>): Any? {
        return context.getByDynamicKey(key)
    }

    override fun toString(): String {
        return "<$key>"
    }
}