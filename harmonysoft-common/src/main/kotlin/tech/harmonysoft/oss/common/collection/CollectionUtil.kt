package tech.harmonysoft.oss.common.collection

inline fun <T, R> Iterable<T>.mapFirstNotNull(transform: (T) -> R?): R? {
    for (item in this) {
        val result = transform(item)
        if (result != null) {
            return result
        }
    }
    return null
}