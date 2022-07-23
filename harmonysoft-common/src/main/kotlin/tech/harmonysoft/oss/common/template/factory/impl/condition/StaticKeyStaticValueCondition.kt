package tech.harmonysoft.oss.common.template.factory.impl.condition

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.util.ObjectUtil

data class StaticKeyStaticValueCondition<K>(
    val key: K,
    val value: Any?
) : Condition<K> {

    override val keys = setOf(key)

    override fun match(context: KeyValueConfigurationContext<K>): Boolean {
        return ObjectUtil.areEqual(context.getByStaticKey(key), value)
    }

    override fun toString(): String {
        return "$key=$value"
    }
}