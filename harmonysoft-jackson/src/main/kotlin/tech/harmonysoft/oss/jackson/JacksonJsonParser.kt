package tech.harmonysoft.oss.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import tech.harmonysoft.oss.json.JsonParser
import javax.inject.Named
import kotlin.reflect.KClass

@Named
class JacksonJsonParser(
    @Json private val mapper: ObjectMapper
) : JsonParser {

    override fun <T : Any> parse(content: String, resultClass: KClass<T>): T {
        return mapper.readValue(content, resultClass.java)
    }
}