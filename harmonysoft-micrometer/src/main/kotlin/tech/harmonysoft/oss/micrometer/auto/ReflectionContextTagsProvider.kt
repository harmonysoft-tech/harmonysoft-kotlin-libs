package tech.harmonysoft.oss.micrometer.auto

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * Utility [ContextTagsProvider] implementation which picks all string constants from the given class
 * and exposes them as [ContextTagsProvider.contextTags]
 */
class ReflectionContextTagsProvider(
    objectClass: KClass<*>,
    vararg toExclude: String
) : ContextTagsProvider {

    override val contextTags = parse(objectClass, *toExclude)

    private fun parse(objectClass: KClass<*>, vararg toExclude: String): Set<String> {
        val toExcludeSet = toExclude.toMutableSet()

        val result = objectClass.memberProperties.mapNotNull { property ->
            if (property.isConst && property.isFinal && !toExcludeSet.remove(property.name)) {
                property.getter.call() as? String
            } else {
                null
            }
        }.toSet()

        if (toExcludeSet.isNotEmpty()) {
            throw IllegalArgumentException(
                "requested to exclude non-existing properties in class ${objectClass.qualifiedName}: $toExcludeSet"
            )
        }

        return result
    }
}