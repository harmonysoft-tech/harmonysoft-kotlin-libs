package tech.harmonysoft.oss.common.template.factory.impl.configurer

import tech.harmonysoft.oss.common.data.DataModificationStrategy
import tech.harmonysoft.oss.common.template.factory.impl.provider.ValueProvider
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer

data class RichStringKeyValueConfigurer<K>(
    val key: K,
    val providers: List<ValueProvider<K>>
) : KeyValueConfigurer<K> {

    override val keys = providers.flatMap { it.keys }.toSet() + key

    override val staticConfiguration = emptyMap<K, Set<Any>>()

    override fun configure(toConfigure: DataModificationStrategy<K>, context: KeyValueConfigurationContext<K>) {
        val value = providers.joinToString(separator = "") {
            it.provide(context).toString()
        }
        toConfigure.setValue(key, value)
    }

    override fun toString(): String {
        return "key=${providers.joinToString(separator = "")}"
    }
}