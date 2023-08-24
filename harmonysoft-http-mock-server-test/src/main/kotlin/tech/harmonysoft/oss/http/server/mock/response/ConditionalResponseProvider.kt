package tech.harmonysoft.oss.http.server.mock.response

import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import tech.harmonysoft.oss.http.server.mock.request.condition.DynamicRequestCondition

data class ConditionalResponseProvider(
    val condition: DynamicRequestCondition,
    val response: HttpResponse
) : ResponseProvider {

    override fun maybeRespond(request: HttpRequest): HttpResponse? {
        return response.takeIf {
            condition.matches(request)
        }
    }
}