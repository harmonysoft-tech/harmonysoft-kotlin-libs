package tech.harmonysoft.oss.http.server.mock.request.condition

import org.mockserver.model.HttpRequest
import tech.harmonysoft.oss.jackson.JsonHelper
import tech.harmonysoft.oss.test.matcher.Matcher

data class JsonBodyPathToMatcherCondition(
    val path2matcher: Map<String, Matcher>,
    val jsonHelper: JsonHelper
) : DynamicRequestCondition {

    override fun matches(request: HttpRequest): Boolean {
        val byPath = jsonHelper.byPath(request.bodyAsJsonOrXmlString)
        return path2matcher.all { (path, matcher) ->
            byPath[path]?.let { actualValue ->
                matcher.matches(actualValue.toString())
            } ?: false
        }
    }

    override fun toString(): String {
        return "target JSON request has the following values: ${path2matcher.entries.joinToString {
            "a value at path '${it.key}' ${it.value}"
        }}"
    }
}