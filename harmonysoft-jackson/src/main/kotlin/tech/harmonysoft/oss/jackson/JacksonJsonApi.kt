package tech.harmonysoft.oss.jackson

import java.io.ByteArrayOutputStream
import javax.inject.Named
import tech.harmonysoft.oss.json.JsonApi
import kotlin.reflect.KClass

@Named
class JacksonJsonApi(
    private val mappers: HarmonysoftJacksonMappers
) : JsonApi {

    override fun <T : Any> parse(content: String, resultClass: KClass<T>): T {
        return mappers.json.readValue(content, resultClass.java)
    }

    override fun writeJson(json: Any): String {
        val bOut = ByteArrayOutputStream()
        bOut.writer().use {
            mappers.json.writeValue(it, json)
        }
        return bOut.toString()
    }
}