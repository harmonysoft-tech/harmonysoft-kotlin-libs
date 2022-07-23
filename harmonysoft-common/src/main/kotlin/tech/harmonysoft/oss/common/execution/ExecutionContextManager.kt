package tech.harmonysoft.oss.common.execution

import kotlin.coroutines.CoroutineContext

/**
 * Holds and exposes information about current execution context in a thread-local storage
 */
interface ExecutionContextManager {

    /**
     * Exposes current context which is configured via previous [pushToContext] calls.
     *
     * Essentially it returns aggregation of [getFromCurrentContext] results for all registered context keys.
     */
    val currentContext: Map<String, Any?>

    /**
     * Exposes all current context information as [CoroutineContext].
     *
     * Currently execution context is based on thread and [ThreadLocal] but different coroutines can be executed
     * on different threads.
     *
     * If we want to keep using [currentContext] data in coroutines, we should include
     * [currentCoroutineContextElements] as part of coroutine context, for example, as below:
     *
     * ```
     * runBlocking {
     *     launch(Dispatchers.IO + executionContextManager.currentCoroutineContextElements) {
     *         // coroutine logic
     *     }
     * }
     * ```
     */
    val currentCoroutineContextElements: CoroutineContext

    fun <T> getFromCurrentContext(key: String): T?

    /**
     * This method and [popFromContext] do the same thing as [withContext], it's highly recommended to use them
     * as below:
     *
     * ```
     * pushToContext(myKey, myValue)
     * try {
     *     // action
     * } finally {
     *     popFromContext(myKey)
     * }
     * ```
     *
     * These methods are introduced in order to avoid new lambda object construction during frequent calls
     * to [withContext].
     *
     * **Note:** it's recommended to use [withContext] whenever possible as it guarantees that target data
     * is popped out from active context once target action is done. This explicit push/pop operations are only
     * for situations when we fine-tune application to avoid memory consumption as much as possible.
     */
    fun pushToContext(key: String, value: Any?)

    /**
     * @see pushToContext
     */
    fun popFromContext(key: String)
}

/**
 * Executes given action in a way that [ExecutionContextManager.getFromCurrentContext] for the given key
 * returns given value until the action is done.
 */
inline fun <T> ExecutionContextManager.withContext(key: String, value: Any?, action: () -> T): T {
    pushToContext(key, value)
    return try {
        action()
    } finally {
        popFromContext(key)
    }
}

/**
 * Similar to `withContext(String, Any?)` but applies the bulk parameters.
 */
inline fun <T> ExecutionContextManager.withContext(context: Map<String, Any?>, action: () -> T): T {
    for ((key, value) in context) {
        pushToContext(key, value)
    }
    return try {
        action()
    } finally {
        for (key in context.keys) {
            popFromContext(key)
        }
    }
}

/**
 * We might start processing in one thread and then continue in another thread, e.g. we might receive an event
 * to process in one thread, queue and process it in a worker thread.
 *
 * That way a [context][withContext] set in the initial thread is lost in the second thread and we need
 * to re-configure it explicitly.
 *
 * This method allows to decorate given action in a way that it's executed in the context which was active
 * during this method's call time.
 *
 * Sample:
 *
 * ```
 * executionContextManager.withContext("some-key", "some-value") {
 *     threadPool.submit(executionContextManager.withCurrentContext {
 *         println(executionContextManager.getFromCurrentContext("some-key")) // is guaranteed to print 'some-value'
 *     })
 * }
 * ```
 */
inline fun <T> ExecutionContextManager.withCurrentContext(crossinline action: () -> T): () -> T {
    val contextToPreserve = currentContext
    return {
        withContext(contextToPreserve) {
            action()
        }
    }
}

inline fun <T, R> ExecutionContextManager.withCurrentContext(crossinline action: (T) -> R): (T) -> R {
    val contextToPreserve = currentContext
    return { param ->
        withContext(contextToPreserve) {
            action(param)
        }
    }
}