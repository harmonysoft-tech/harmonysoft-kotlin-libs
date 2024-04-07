package tech.harmonysoft.oss.http.server.mock.request.impl

import org.mockserver.model.HttpRequest
import tech.harmonysoft.oss.http.server.mock.request.ExpectedRequestConfigurer
import jakarta.inject.Named

@Named
class PathConfigurer : ExpectedRequestConfigurer {

    override val criteriaName = "path"

    override fun configure(request: HttpRequest, criteriaValue: String): HttpRequest {
        return request.withPath(criteriaValue)
    }
}