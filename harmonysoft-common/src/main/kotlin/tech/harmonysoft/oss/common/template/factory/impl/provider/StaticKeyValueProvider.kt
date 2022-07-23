package tech.harmonysoft.oss.common.template.factory.impl.provider

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext

data class StaticKeyValueProvider<K>(
    val key: K
) : ValueProvider<K> {

    override val keys = setOf(key)

    override fun provide(context: KeyValueConfigurationContext<K>): Any? {
        return context.getByStaticKey(key)
    }

    override fun toString(): String {
        return "<$key>"
    }
}