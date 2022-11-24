package tech.harmonysoft.oss.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import javax.inject.Named

@Named
class JsonHelper(
    @Json private val mapper: ObjectMapper
) {

    fun byPath(json: String): Map<String, Any> {
        val asMap = mapper.readValue(json, Map::class.java)
        val result = mutableMapOf<String, Any>()
        for ((key, value) in asMap) {
            if (value is Map<*, *> || value is Collection<*>) {
                fill(result, key.toString(), value)
            } else if (value != null) {
                result[key.toString()] = value
            }
        }
        return result
    }

    private fun fill(holder: MutableMap<String, Any>, path: String, value: Any?) {
        when {
            value is Map<*, *> -> value.forEach { key, v ->
                fill(holder, "$path.$key", v)
            }
            value is Collection<*> -> value.forEachIndexed { i, v ->
                fill(holder, "$path[$i]", v)
            }
            value != null -> holder[path] = value
        }
    }
}