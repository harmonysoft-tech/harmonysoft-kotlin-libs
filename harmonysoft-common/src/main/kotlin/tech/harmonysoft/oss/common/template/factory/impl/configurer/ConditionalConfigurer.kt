package tech.harmonysoft.oss.common.template.factory.impl.configurer

import tech.harmonysoft.oss.common.template.factory.impl.condition.Condition
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer

data class ConditionalConfigurer<K>(
    val condition: Condition<K>,
    val configurer: KeyValueConfigurer<K>
) {

    override fun toString(): String {
        return "if $condition then $configurer"
    }
}