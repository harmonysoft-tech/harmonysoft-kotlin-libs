package tech.harmonysoft.oss.common.template.factory.impl.condition

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.util.ObjectUtil

data class StaticKeyDynamicValueKeyCondition<K>(
    val key: K,
    val dynamicKey: String
) : Condition<K> {

    override val keys = setOf(key)

    override fun match(context: KeyValueConfigurationContext<K>): Boolean {
        return ObjectUtil.areEqual(context.getByStaticKey(key), context.getByDynamicKey(dynamicKey))
    }

    override fun toString(): String {
        return "$key=<$dynamicKey>"
    }
}