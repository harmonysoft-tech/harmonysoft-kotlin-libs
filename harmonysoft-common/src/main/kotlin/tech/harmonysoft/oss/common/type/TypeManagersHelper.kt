package tech.harmonysoft.oss.common.type

import javax.inject.Named
import kotlin.reflect.KClass

@Named
class TypeManagersHelper(
    _managers: Collection<TypeManager<*>>
) {

    private val managers = _managers.groupBy {
        Key(it.targetType, it.targetContext)
    }.mapValues { (key, typeManagers) ->
        if (typeManagers.size > 1) {
            throw IllegalArgumentException(
                "more than one ${TypeManager::class.qualifiedName} is found for type ${key.type.qualifiedName} "
                + "and context ${key.context}: ${typeManagers.map { it::class.qualifiedName }}"
            )
        } else {
            typeManagers.first()
        }
    }

    fun <T : Any> getTypeManager(type: KClass<T>): TypeManager<T> {
        return getTypeManager(type, DEFAULT_CONTEXTS)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getTypeManager(type: KClass<T>, contexts: Set<TypeManagerContext>): TypeManager<T> {
        if (contexts.isEmpty()) {
            throw IllegalArgumentException(
                "can't get a ${TypeManager::class.simpleName} for type ${type.qualifiedName} and no context"
            )
        }

        val candidates = contexts.mapNotNull {
            managers[Key(type, it)] as? TypeManager<T>
        }

        return when (candidates.size) {
            0 -> throw IllegalArgumentException(
                "can't find a ${TypeManager::class.simpleName} for type ${type.qualifiedName} and contexts "
                + "$contexts. Available: $managers"
            )
            1 -> candidates.first()
            2 -> if (candidates.any { it.targetContext == TypeManagerContext.DEFAULT}) {
                // if there is a type manager for the default context and for a specific context,
                // we choose the one for non-default context
                candidates.first { it.targetContext != TypeManagerContext.DEFAULT }
            } else {
                throw IllegalArgumentException(
                    "more than one ${TypeManager::class.simpleName} is found for type ${type.qualifiedName} "
                    + "and contexts $contexts: $candidates"
                )
            }
            else -> throw IllegalArgumentException(
                "more than one ${TypeManager::class.simpleName} is found for type ${type.qualifiedName} "
                + "and contexts $contexts: $candidates"
            )
        }
    }

    companion object {
        val DEFAULT_CONTEXTS = setOf(TypeManagerContext.DEFAULT)
    }

    private data class Key(val type: KClass<*>, val context: TypeManagerContext)
}