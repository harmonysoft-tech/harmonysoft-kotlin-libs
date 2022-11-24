package tech.harmonysoft.oss.http.server.mock.response

import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

fun interface ResponseProvider {

    fun maybeRespond(request: HttpRequest): HttpResponse?
}