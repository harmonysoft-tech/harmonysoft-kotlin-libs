package tech.harmonysoft.oss.http.client

import org.apache.hc.client5.http.classic.methods.*
import org.apache.hc.core5.http.HttpEntity
import tech.harmonysoft.oss.http.client.response.HttpResponse
import tech.harmonysoft.oss.http.client.response.HttpResponseConverter

interface HttpClient {

    fun <T> execute(
        request: HttpUriRequestBase,
        converter: HttpResponseConverter<T>,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse<T>

    fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse<String> {
        return get(url, HttpResponseConverter.STRING, headers)
    }

    fun <T> get(
        url: String,
        converter: HttpResponseConverter<T>,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse<T> {
        return execute(HttpGet(url), converter, headers)
    }

    fun <T> post(
        url: String,
        entity: HttpEntity,
        converter: HttpResponseConverter<T>,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse<T> {
        return execute(HttpPost(url).apply { this.entity = entity }, converter, headers)
    }

    fun <T> put(
        url: String,
        entity: HttpEntity,
        converter: HttpResponseConverter<T>,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse<T> {
        return execute(HttpPut(url).apply { this.entity = entity }, converter, headers)
    }

    fun <T> delete(
        url: String,
        converter: HttpResponseConverter<T>,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse<T> {
        return execute(HttpDelete(url), converter, headers)
    }

    fun close()
}