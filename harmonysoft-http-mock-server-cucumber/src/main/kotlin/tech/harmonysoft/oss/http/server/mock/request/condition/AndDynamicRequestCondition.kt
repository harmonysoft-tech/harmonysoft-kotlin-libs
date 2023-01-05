package tech.harmonysoft.oss.http.server.mock.request.condition

import org.mockserver.model.HttpRequest

data class AndDynamicRequestCondition(
    val first: DynamicRequestCondition,
    val second: DynamicRequestCondition
) : DynamicRequestCondition {

    override fun matches(request: HttpRequest): Boolean {
        return first.matches(request) && second.matches(request)
    }

    override fun toString(): String {
        return "$first and $second"
    }
}