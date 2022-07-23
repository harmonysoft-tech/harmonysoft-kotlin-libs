package tech.harmonysoft.oss.common.template.factory.impl.provider

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer

interface ValueProvider<K> {

    /**
     * Set of keys which might be [asked][KeyValueConfigurationContext.getByStaticKey] from the context
     * during [target value configuration][KeyValueConfigurer.configure]
     */
    val keys: Set<K>

    fun provide(context: KeyValueConfigurationContext<K>): Any?
}