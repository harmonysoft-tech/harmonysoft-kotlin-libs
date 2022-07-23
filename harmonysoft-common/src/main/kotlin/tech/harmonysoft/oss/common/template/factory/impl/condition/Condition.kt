package tech.harmonysoft.oss.common.template.factory.impl.condition

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer

interface Condition<K> {

    /**
     * Set of keys which might be [asked][KeyValueConfigurationContext.getByStaticKey] from the context
     * during [target value configuration][KeyValueConfigurer.configure]
     */
    val keys: Set<K>

    fun match(context: KeyValueConfigurationContext<K>): Boolean

    companion object {

        private val MATCH_ALL = object : Condition<Any> {

            override val keys = emptySet<Any>()

            override fun match(context: KeyValueConfigurationContext<Any>): Boolean {
                return true
            }

            override fun toString(): String {
                return "<always>"
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun <K> matchAll(): Condition<K> {
            return MATCH_ALL as Condition<K>
        }
    }
}