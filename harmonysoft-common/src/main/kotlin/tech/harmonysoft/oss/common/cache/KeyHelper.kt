package tech.harmonysoft.oss.common.cache

import java.util.concurrent.ConcurrentHashMap

/**
 * Utility class for a situation when we need to frequently use an immutable key (e.g. for checking shared
 * [ConcurrentHashMap] content). We want to avoid creating new key objects all the time if a number of possible
 * different key states is low and a number of threads which do the operation is also not big.
 *
 * The general idea is to do the following:
 *
 * 1. Keep thread-local mutable key
 * 2. Get a mutable key any time new immutable key is required
 * 3. Modify mutable key within the current thread-local data
 * 4. Keep a cache of mutable key -> immutable key
 * 5. Query target immutable key by mutable key
 * 6. Create new immutable key on the basis of the mutable key and store it if the cache doesn't
 *    have corresponding entry yet
 */
class KeyHelper<IMMUTABLE_KEY, MUTABLE_KEY>(
    private val keyConverter: (MUTABLE_KEY) -> IMMUTABLE_KEY,
    mutableKeyCreator: () -> MUTABLE_KEY
) {

    private val immutableKeyCreator: (MUTABLE_KEY) -> IMMUTABLE_KEY = {
        keyConverter(it)
    }

    private val keysCache = ThreadLocal.withInitial {
        mutableMapOf<MUTABLE_KEY, IMMUTABLE_KEY>()
    }
    private val mutableKeyThreadLocal = ThreadLocal.withInitial(mutableKeyCreator)

    val mutableKey: MUTABLE_KEY
        get() = mutableKeyThreadLocal.get()

    fun getImmutableKey(key: MUTABLE_KEY): IMMUTABLE_KEY {
        val cache = keysCache.get()
        return cache[key] ?: cache.computeIfAbsent(key, immutableKeyCreator)
    }
}