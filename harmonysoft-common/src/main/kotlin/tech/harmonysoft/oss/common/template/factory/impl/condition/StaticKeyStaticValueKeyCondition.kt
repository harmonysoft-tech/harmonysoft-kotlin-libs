package tech.harmonysoft.oss.common.template.factory.impl.condition

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.util.ObjectUtil

data class StaticKeyStaticValueKeyCondition<K>(
    val key: K,
    val staticKey: K
) : Condition<K> {

    override val keys = setOf(key, staticKey)

    override fun match(context: KeyValueConfigurationContext<K>): Boolean {
        return ObjectUtil.areEqual(context.getByStaticKey(key), context.getByStaticKey(staticKey))
    }

    override fun toString(): String {
        return "$key=<$staticKey>"
    }
}