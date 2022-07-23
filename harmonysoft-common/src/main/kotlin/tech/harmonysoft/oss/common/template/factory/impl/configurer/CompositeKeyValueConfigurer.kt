package tech.harmonysoft.oss.common.template.factory.impl.configurer

import tech.harmonysoft.oss.common.collection.CollectionInitializer
import tech.harmonysoft.oss.common.data.DataModificationStrategy
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer

class CompositeKeyValueConfigurer<K>(
    val configurers: Array<KeyValueConfigurer<K>>
) : KeyValueConfigurer<K> {

    override val keys = configurers.flatMap { it.keys }.toSet()

    override val staticConfiguration = configurers.fold(mutableMapOf<K, MutableSet<Any>>()) { acc, configurer ->
        acc.apply {
            for ((key, value) in configurer.staticConfiguration) {
                getOrPut(key, CollectionInitializer.mutableSet()).addAll(value)
            }
        }
    }

    override fun configure(toConfigure: DataModificationStrategy<K>, context: KeyValueConfigurationContext<K>) {
        for (configurer in configurers) {
            configurer.configure(toConfigure, context)
        }
    }

    override fun hashCode(): Int {
        return configurers.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is CompositeKeyValueConfigurer<*> && configurers.contentEquals(other.configurers)
    }

    override fun toString(): String {
        return configurers.joinToString()
    }
}