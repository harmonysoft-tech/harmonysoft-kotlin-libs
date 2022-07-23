package tech.harmonysoft.oss.common.cache

/**
 * Normally it's ok to create short-lived objects and let GC take care of them. However, sometimes we have
 * strict requirements for application latency (the case in high frequency electronic trading).
 *
 * In this situation it might be desirable to have a pool of objects and reuse them to avoid memory consumption,
 * thus reducing GC impact.
 *
 * That design goes great with disruptor pattern when new task in enqueued to a worker thread in a thread-safe
 * manner and all further processing is thread-local (lock-free).
 *
 * This class is a simple objects pool.
 *
 * **Note:** this class is **not** thread-safe.
 */
class SimpleObjectsPool<T>(
    private val initializer: () -> T,
    private val resetter: (T) -> Unit
) {

    private val pool = mutableListOf<T>()

    val next: T
        get() {
            return if (pool.isEmpty()) {
                initializer()
            } else {
                pool.removeLast()
            }
        }

    fun restore(item: T) {
        resetter(item)
        pool += item
    }
}