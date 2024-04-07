package tech.harmonysoft.oss.http.server.mock.request.impl

import org.mockserver.model.HttpRequest
import tech.harmonysoft.oss.http.server.mock.request.ExpectedRequestConfigurer
import jakarta.inject.Named

@Named
class MethodConfigurer : ExpectedRequestConfigurer {

    override val criteriaName = "method"

    override fun configure(request: HttpRequest, criteriaValue: String): HttpRequest {
        return request.withMethod(criteriaValue)
    }
}