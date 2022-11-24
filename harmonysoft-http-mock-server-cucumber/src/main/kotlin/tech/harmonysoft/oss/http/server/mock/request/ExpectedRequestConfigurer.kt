package tech.harmonysoft.oss.http.server.mock.request

import org.mockserver.model.HttpRequest

interface ExpectedRequestConfigurer {

    val criteriaName: String

    fun configure(request: HttpRequest, criteriaValue: String): HttpRequest
}