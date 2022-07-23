package tech.harmonysoft.oss.common.data

/**
 * Defines an interface for an entity which holds key-value pairs and allows adding/modifying them.
 */
interface DataModificationStrategy<K> {

    fun setValue(key: K, value: Any?)
}