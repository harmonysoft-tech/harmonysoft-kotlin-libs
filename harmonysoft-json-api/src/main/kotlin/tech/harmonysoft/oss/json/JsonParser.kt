package tech.harmonysoft.oss.json

import kotlin.reflect.KClass

interface JsonParser {

    fun <T : Any> parse(content: String, resultClass: KClass<T>): T
}