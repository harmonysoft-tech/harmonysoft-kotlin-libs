package tech.harmonysoft.oss.common.template.factory.impl.condition

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.util.ObjectUtil

data class DynamicKeyDynamicValueCondition<K>(
    val key: String,
    val valueKey: String
) : Condition<K> {

    override val keys = emptySet<K>()

    override fun match(context: KeyValueConfigurationContext<K>): Boolean {
        return ObjectUtil.areEqual(context.getByDynamicKey(key), context.getByDynamicKey(valueKey))
    }

    override fun toString(): String {
        return "<$key>=<$valueKey"
    }
}