package tech.harmonysoft.oss.http.server.mock.request.condition

import org.mockserver.model.HttpRequest

data class ParameterName2ValueCondition(
    val expected: Map<String, String>
) : DynamicRequestCondition {

    override fun matches(request: HttpRequest): Boolean {
        return expected.all { (name, value) ->
            request.hasQueryStringParameter(name, value)
        }
    }

    override fun toString(): String {
        return "request has the following query parameters: ${expected.entries.joinToString {
            "${it.key}=${it.value}"
        }}"
    }

}