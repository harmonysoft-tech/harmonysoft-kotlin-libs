package tech.harmonysoft.oss.common.template.factory.impl.condition

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.util.ObjectUtil

data class DynamicKeyStaticValueCondition<K>(
    val key: String,
    val value: Any?
) : Condition<K> {

    override val keys = emptySet<K>()

    override fun match(context: KeyValueConfigurationContext<K>): Boolean {
        return ObjectUtil.areEqual(context.getByDynamicKey(key), value)
    }

    override fun toString(): String {
        return "<$key>=$value"
    }
}