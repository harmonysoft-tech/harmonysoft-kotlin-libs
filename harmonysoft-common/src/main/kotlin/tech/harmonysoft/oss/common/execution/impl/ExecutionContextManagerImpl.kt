package tech.harmonysoft.oss.common.execution.impl

import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.slf4j.MDCContext
import org.slf4j.MDC
import tech.harmonysoft.oss.common.collection.CollectionInitializer
import tech.harmonysoft.oss.common.execution.ExecutionContextManager
import tech.harmonysoft.oss.common.reflection.TypeUtil.PRIMITIVES
import java.util.*
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

@Named
class ExecutionContextManagerImpl : ExecutionContextManager {

    private val context = ThreadLocal.withInitial { mutableMapOf<String, Stack<Any>>() }
    private val previousMdcValues = ThreadLocal.withInitial { mutableMapOf<String, Stack<String?>>() }

    override val currentCoroutineContextElements: CoroutineContext
        get() = context.asContextElement(copyContext(context)) +
                previousMdcValues.asContextElement(copyContext(previousMdcValues)) + MDCContext()

    override val currentContext: Map<String, Any?>
        get() = context.get().mapValues { (_, value) ->
            if (value.isEmpty()) {
                null
            } else {
                value.peek()
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun <T> copyContext(threadLocal: ThreadLocal<MutableMap<String, Stack<T>>>): MutableMap<String, Stack<T>> {
        return threadLocal.get().map { (key, value) ->
            key to value.clone() as Stack<T>
        }.toMap().toMutableMap()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getFromCurrentContext(key: String): T? {
        val stack = context.get()[key]
        return if (stack.isNullOrEmpty()) {
            null
        } else {
            stack.peek() as T?
        }
    }

    override fun pushToContext(key: String, value: Any?) {
        context.get().getOrPut(key, CollectionInitializer.stack()).push(value)
        if (value != null && PRIMITIVES.contains(value::class)) {
            previousMdcValues.get().getOrPut(key, CollectionInitializer.stack()).push(MDC.get(key))
            MDC.put(key, value.toString())
        }
    }

    override fun popFromContext(key: String) {
        val contextValues = context.get().getOrPut(key, CollectionInitializer.stack())
        if (contextValues.isEmpty()) {
            throw IllegalArgumentException("can't pop context value for kyey '$key' - no value is registered for it")
        }

        val removedValue = contextValues.pop()
        if (removedValue != null && PRIMITIVES.contains(removedValue::class)) {
            val mdcValues = previousMdcValues.get().getOrPut(key, CollectionInitializer.stack())
            if (mdcValues.isEmpty()) {
                throw IllegalStateException(
                    "can't restore MDC value for key '$key' - no previous value is registered for it"
                )
            }
            val previousMdcValue = mdcValues.pop()
            if (previousMdcValue == null) {
                MDC.remove(key)
            } else {
                MDC.put(key, previousMdcValue)
            }
        }
    }
}