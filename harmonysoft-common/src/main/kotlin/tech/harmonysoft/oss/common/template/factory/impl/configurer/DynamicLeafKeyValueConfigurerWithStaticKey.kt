package tech.harmonysoft.oss.common.template.factory.impl.configurer

import tech.harmonysoft.oss.common.data.DataModificationStrategy
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer

data class DynamicLeafKeyValueConfigurerWithStaticKey<K>(
    val keyToConfigure: K,
    val dynamicValueKey: K
) : KeyValueConfigurer<K> {

    override val keys = setOf(keyToConfigure, dynamicValueKey)

    override val staticConfiguration = emptyMap<K, Set<Any>>()

    override fun configure(toConfigure: DataModificationStrategy<K>, context: KeyValueConfigurationContext<K>) {
        val value = context.getByStaticKey(dynamicValueKey)
        toConfigure.setValue(keyToConfigure, value)
    }

    override fun toString(): String {
        return "$keyToConfigure=<$dynamicValueKey>"
    }
}