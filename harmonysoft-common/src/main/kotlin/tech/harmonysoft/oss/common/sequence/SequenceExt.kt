package tech.harmonysoft.oss.common.sequence

inline fun <T, R> Sequence<T>.mapFirstNotNull(transform: (T) -> R?): R? {
    for (item in this) {
        val result = transform(item)
        if (result != null) {
            return result
        }
    }
    return null
}