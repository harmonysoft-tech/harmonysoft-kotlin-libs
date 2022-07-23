package tech.harmonysoft.oss.common.data

import kotlin.reflect.KClass

/**
 * We have a common use-case when there are a number of key-value pairs where value type is implied by the key,
 * i.e. all possible values of particular key have the same type.
 *
 * This interface defines common strategy for representing such key's management.
 */
interface TypedKeyManager<KEY> {

    fun getValueType(key: KEY): KClass<*>

    /**
     * Quite often we want keys to be defined in configs, and it's also not rare for configs to be text files
     * (e.g. YAML files). So, we might need to map particular key's string representation to the key object.
     *
     * This method allows to do such key parsing.
     */
    fun parseKey(raw: String): KEY
}