package tech.harmonysoft.oss.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.ByteArrayOutputStream
import tech.harmonysoft.oss.json.JsonApi
import javax.inject.Named
import kotlin.reflect.KClass

@Named
class JacksonJsonApi(
    @Json private val mapper: ObjectMapper
) : JsonApi {

    override fun <T : Any> parse(content: String, resultClass: KClass<T>): T {
        return mapper.readValue(content, resultClass.java)
    }

    override fun writeJson(json: Any): String {
        val bOut = ByteArrayOutputStream()
        bOut.writer().use {
            mapper.writeValue(it, json)
        }
        return bOut.toString()
    }
}