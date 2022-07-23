package tech.harmonysoft.oss.common.template.factory.impl.configurer

import tech.harmonysoft.oss.common.data.DataModificationStrategy
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer

data class StaticLeafKeyValueConfigurer<K>(
    val key: K,
    val value: Any?
) : KeyValueConfigurer<K> {

    override val keys = setOf(key)

    override val staticConfiguration = value?.let {
        mapOf(key to setOf(it))
    } ?: emptyMap()

    override fun configure(toConfigure: DataModificationStrategy<K>, context: KeyValueConfigurationContext<K>) {
        toConfigure.setValue(key, value)
    }

    override fun toString(): String {
        return "$key=$value"
    }
}