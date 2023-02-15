package tech.harmonysoft.oss.json

import kotlin.reflect.KClass

interface JsonParser {

    fun <T : Any> parse(content: String, resultClass: KClass<T>): T

    fun parseJson(json: String): Any {
        // it might be a regular json or json array, that's why we try to parse it as a map first and fallback to list
        return try {
            parse(json, Map::class)
        } catch (_: Exception) {
            try {
                parse(json, List::class)
            } catch (_: Exception) {
                throw IllegalArgumentException("can't parse JSON from\n$json")
            }
        }
    }

}