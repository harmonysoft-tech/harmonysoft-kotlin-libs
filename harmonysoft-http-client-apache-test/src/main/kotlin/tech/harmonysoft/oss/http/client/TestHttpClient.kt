package tech.harmonysoft.oss.http.client

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase
import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.di.DiConstants
import tech.harmonysoft.oss.http.client.impl.HttpClientImpl
import tech.harmonysoft.oss.http.client.response.HttpResponse
import tech.harmonysoft.oss.http.client.response.HttpResponseConverter
import tech.harmonysoft.oss.test.TestAware
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.Priority
import javax.inject.Named

@Priority(DiConstants.LIB_PRIMARY_PRIORITY)
@Named
class TestHttpClient(
    private val delegate: HttpClientImpl
) : HttpClient, TestAware {

    private val logger = LoggerFactory.getLogger(TestHttpClient::class.java)
    private val stubResponses = LinkedBlockingQueue<ResponseStub>()

    override fun onTestEnd() {
        stubResponses.clear()
    }

    fun stubResponse(method: String, urlPattern: Regex, response: HttpResponse<ByteArray>) {
        stubResponses.removeIf {
            it.method == method && it.urlPattern.toString() == urlPattern.toString()
        }
        stubResponses += ResponseStub(method, urlPattern, response)
    }

    private fun maybeFindStubResponse(method: String, url: String): HttpResponse<ByteArray>? {
        return stubResponses.find { stub ->
            stub.method == method && stub.urlPattern.matches(url)
        }?.response ?: run {
            logger.info("No stub response is detected for {} request to {}, performing a real HTTP call", method, url)
            null
        }
    }

    override fun <T> execute(
        request: HttpUriRequestBase,
        converter: HttpResponseConverter<T>,
        headers: Map<String, String>
    ): HttpResponse<T> {
        return maybeFindStubResponse(request.method, request.requestUri)?.let {
            it.withBody(converter.convert(it.body, Charsets.UTF_8))
        } ?: delegate.execute(request, converter, headers)
    }

    override fun close() {
    }

    private data class ResponseStub(
        val method: String,
        val urlPattern: Regex,
        val response: HttpResponse<ByteArray>
    )
}