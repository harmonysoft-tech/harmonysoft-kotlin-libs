package tech.harmonysoft.oss.common.array

inline fun <T, R> Array<T>.mapFirstNotNull(transform: (T) -> R?): R? {
    for (item in this) {
        transform(item)?.let { return it }
    }
    return null
}