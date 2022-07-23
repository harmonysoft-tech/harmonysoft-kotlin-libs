package tech.harmonysoft.oss.http.client

import org.apache.hc.core5.http.HttpRequest
import org.apache.hc.core5.http.HttpResponse

interface HttpListener {

    fun onRequest(request: HttpRequest)

    fun onResponse(request: HttpRequest, response: HttpResponse)
}