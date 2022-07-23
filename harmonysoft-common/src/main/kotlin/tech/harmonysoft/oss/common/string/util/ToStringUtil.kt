package tech.harmonysoft.oss.common.string.util

import tech.harmonysoft.oss.common.reflection.TypeUtil
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HideValueInToString

object ToStringUtil {

    const val HIDDEN_VALUE_PLACEHOLDER = "***"

    /**
     * Convenient way to build object's string representation where some fields should be excluded (e.g. a field
     * which holds sensitive data like password)
     */
    fun build(o: Any?): String {
        o ?: return "null"
        if (TypeUtil.PRIMITIVES.contains(o::class)) {
            return o.toString()
        }
        when (o) {
            is Class<*> -> return "class ${o.name}"
            is KClass<*> -> return "class ${o.simpleName}"
            is KFunction<*> -> return "fun ${o.name}()"
            is Collection<*> -> return o.joinToString(prefix = "[", postfix = "]") {
                build(it)
            }
            is Array<*> -> return o.joinToString(prefix = "[", postfix = "]") {
                build(it)
            }
            is Map<*, *> -> return o.entries.joinToString(prefix = "{", postfix = "}") {
                "${build(it.key)}: ${build(it.value)}"
            }
        }
        val memberProperties = o::class.memberProperties.sortedBy { it.name }
        if (memberProperties.isEmpty()) {
            return o::class.simpleName ?: throw IllegalArgumentException("can't build string representation for $o")
        }
        val values = memberProperties.mapNotNull { property ->
            if (property.visibility != KVisibility.PUBLIC) {
                null
            } else {
                val value = if (property.findAnnotation<HideValueInToString>() != null) {
                    HIDDEN_VALUE_PLACEHOLDER
                } else {
                    property.call(o)
                }
                "${property.name}=${build(value)}"
            }
        }
        return values.joinToString(prefix = "(", postfix = ")")
    }
}