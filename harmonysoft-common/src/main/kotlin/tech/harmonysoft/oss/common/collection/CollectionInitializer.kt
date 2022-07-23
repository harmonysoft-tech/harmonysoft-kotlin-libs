package tech.harmonysoft.oss.common.collection

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Quite often many lambda objects are generated during frequent calls like [getOrPut]:
 *
 * ```
 * class MyClass {
 *     private val cache = mutableMapOf<String, Stack<MyData>>()
 *
 *     fun service(key: String) {
 *         cache.getOrPut(key) { Stack<MyData>() }
 *     }
 * }
 * ```
 *
 * That way it's more effective pre-defining initializers and use them like below - by doing that we avoie
 * unnecessary lambda objects construction:
 *
 * ```
 * cache.getOrPut(key, CollectionInitializer.stack())
 * ```
 */
@Suppress("UNCHECKED_CAST")
object CollectionInitializer {

    private val STACK: () -> Stack<Any> = { Stack() }
    private val MUTABLE_LIST: () -> MutableList<Any> = { mutableListOf() }
    private val MUTABLE_SET: () -> MutableSet<Any> = { mutableSetOf() }
    private val MUTABLE_MAP: () -> MutableMap<Any, Any> = { mutableMapOf() }
    private val CONCURRENT_HASH_SET: () -> MutableSet<Any> = { Collections.newSetFromMap(ConcurrentHashMap()) }
    private val CONCURRENT_HASH_MAP: () -> ConcurrentHashMap<Any, Any> = { ConcurrentHashMap() }

    fun <T> stack(): () -> Stack<T> = STACK as () -> Stack<T>
    fun <T> mutableList(): () -> MutableList<T> = MUTABLE_LIST as () -> MutableList<T>
    fun <T> mutableSet(): () -> MutableSet<T> = MUTABLE_SET as () -> MutableSet<T>
    fun <K, V> mutableMap(): () -> MutableMap<K, V> = MUTABLE_MAP as () -> MutableMap<K, V>
    fun <T> concurrentHashSet(): () -> MutableSet<T> = CONCURRENT_HASH_SET as () -> MutableSet<T>
    fun <K, V> concurrentHashMap(): () -> ConcurrentHashMap<K, V> = CONCURRENT_HASH_MAP as () -> ConcurrentHashMap<K, V>
}