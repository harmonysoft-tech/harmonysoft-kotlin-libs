package tech.harmonysoft.oss.common.template.factory.impl.condition

import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext

class OrCondition<K>(
    val conditions: Array<Condition<K>>
) : Condition<K> {

    override val keys = conditions.flatMap { it.keys }.toSet()

    override fun match(context: KeyValueConfigurationContext<K>): Boolean {
        return conditions.any {
            it.match(context)
        }
    }

    override fun hashCode(): Int {
        return conditions.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is OrCondition<*> && conditions.contentEquals(other.conditions)
    }

    override fun toString(): String {
        return conditions.joinToString(prefix = "(", postfix = ")", separator = " or ")
    }
}