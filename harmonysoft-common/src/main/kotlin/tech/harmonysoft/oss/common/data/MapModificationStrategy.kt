package tech.harmonysoft.oss.common.data

object MapModificationStrategy : DataModificationStrategy<Any> {

    val MAP = ThreadLocal<MutableMap<Any, Any?>>()

    override fun setValue(key: Any, value: Any?) {
        MAP.get()[key] = value
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <K, R> MapModificationStrategy.forMap(
    map: MutableMap<K, Any?>,
    callback: (DataModificationStrategy<K>) -> R
): R {
    MAP.set(map as MutableMap<Any, Any?>)
    return try {
        callback(MapModificationStrategy as DataModificationStrategy<K>)
    } finally {
        MAP.set(null)
    }
}