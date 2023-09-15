package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.java.After
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import org.apache.hc.client5.http.classic.methods.HttpDelete
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpHead
import org.apache.hc.client5.http.classic.methods.HttpOptions
import org.apache.hc.client5.http.classic.methods.HttpPatch
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.classic.methods.HttpPut
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase
import org.apache.hc.client5.http.entity.mime.ByteArrayBody
import org.apache.hc.client5.http.entity.mime.ContentBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpHeaders
import org.apache.hc.core5.http.HttpStatus
import org.apache.hc.core5.http.io.entity.StringEntity
import org.assertj.core.api.Assertions.assertThat
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.common.collection.CollectionInitializer
import tech.harmonysoft.oss.http.client.TestHttpClient
import tech.harmonysoft.oss.http.client.config.DefaultWebPortProvider
import tech.harmonysoft.oss.http.client.fixture.HttpClientTestFixture
import tech.harmonysoft.oss.http.client.response.HttpResponse
import tech.harmonysoft.oss.http.client.response.HttpResponseConverter
import tech.harmonysoft.oss.json.JsonApi
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.content.TestContentManager
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.json.CommonJsonUtil
import tech.harmonysoft.oss.test.util.TestUtil.fail

class HttpClientStepDefinitions {

    private val responses = ConcurrentHashMap<String/* http method */, MutableList<ResponseEntry>>()
    private val defaultHostName = AtomicReference(DEFAULT_HOST_NAME)
    private val commonHeaders = ConcurrentHashMap<String, String>()

    @Inject private lateinit var defaultPortProvider: DefaultWebPortProvider
    @Inject private lateinit var fixtureDataHelper: FixtureDataHelper
    @Inject private lateinit var httpClient: TestHttpClient
    @Inject private lateinit var contentManager: TestContentManager
    @Inject private lateinit var jsonApi: JsonApi
    @Inject private lateinit var dynamicContext: DynamicBindingContext

    private val requestBuilders = mapOf<String, (String) -> HttpUriRequestBase> (
        HttpGet.METHOD_NAME to { HttpGet(it) },
        HttpPost.METHOD_NAME to { HttpPost(it) },
        HttpPut.METHOD_NAME to { HttpPut(it) },
        HttpDelete.METHOD_NAME to { HttpDelete(it) },
        HttpHead.METHOD_NAME to { HttpHead(it) },
        HttpOptions.METHOD_NAME to { HttpOptions(it) },
        HttpPatch.METHOD_NAME to { HttpPatch(it) },
    )

    @After
    fun tearDown() {
        defaultHostName.set(DEFAULT_HOST_NAME)
        responses.clear()
        commonHeaders.clear()
    }

    @Given("^common HTTP header is defined: ([^\\s]+)=([^\\s]+)$")
    fun addCommonHeader(key: String, value: String) {
        commonHeaders[key] = value
    }

    @Given("^HTTP ([^\\s]+) request to ([^\\s]+) is made$")
    fun makeRequest(httpMethod: String, urlOrPath: String) {
        val url = getFullUrl(urlOrPath)
        val request = getRequest(httpMethod, url)
        val response = httpClient.execute(request, HttpResponseConverter.BYTE_ARRAY, commonHeaders)
        onResponse(url, httpMethod, response)
    }

    private fun getRequest(httpMethod: String, url: String): HttpUriRequestBase {
        return requestBuilders[httpMethod]?.invoke(url) ?: fail(
            "unknown HTTP method '$httpMethod', supported methods: ${requestBuilders.keys.joinToString()}"
        )
    }

    @Given("^HTTP ([^\\s]+) request to ([^\\s]+) is made with headers '([^']+)'$")
    fun makeRequestWithHeaders(httpMethod: String, urlOrPath: String, rawHeaders: String) {
        val url = getFullUrl(urlOrPath)
        val headers = commonHeaders + parseHeaders(rawHeaders)
        val request = getRequest(httpMethod, url)
        val response = httpClient.execute(request, HttpResponseConverter.BYTE_ARRAY, headers)
        onResponse(url, httpMethod, response)
    }

    private fun parseHeaders(raw: String): Map<String, String> {
        return raw
            .split(",")
            .map(String::trim)
            .map {
                val i = it.indexOf('=')
                if (i <= 0) {
                    fail("incorrect HTTP header '$it', full headers string: '$raw'")
                }
                it.substring(0, i) to it.substring(i + 1)
            }.toMap()
    }

    @Given("^HTTP ([^\\s]+) request to ([^\\s]+) is made with JSON body:$")
    fun makeRequestWithJsonBody(httpMethod: String, urlOrPath: String, json: String) {
        val url = getFullUrl(urlOrPath)
        val headers = mapOf(HttpHeaders.CONTENT_TYPE to ContentType.APPLICATION_JSON.mimeType) + commonHeaders
        val request = getRequest(httpMethod, url)
        val expandedJson = fixtureDataHelper.prepareTestData(HttpClientTestFixture.TYPE, Unit, json)
        request.entity = StringEntity(expandedJson.toString())
        val response = httpClient.execute(request, HttpResponseConverter.BYTE_ARRAY, headers)
        onResponse(url, httpMethod, response)
    }

    @Given("^HTTP ([^\\s]+) request to ([^\\s]+) is made with headers '([^']+)' and JSON body:$")
    fun makeRequestHeadersAndJsonBody(httpMethod: String, urlOrPath: String, headersString: String, json: String) {
        val url = getFullUrl(urlOrPath)
        val headers = commonHeaders +
                      mapOf(HttpHeaders.CONTENT_TYPE to ContentType.APPLICATION_JSON.mimeType) +
                      parseHeaders(headersString)
        val request = getRequest(httpMethod, url)
        request.entity = StringEntity(json)
        val response = httpClient.execute(request, HttpResponseConverter.BYTE_ARRAY, headers)
        onResponse(url, HttpPost.METHOD_NAME, response)
    }

    @Given("^file ([^\\s]+) is uploaded as part ([^\\s]+) using HTTP ([^\\s]+) to ([^\\s]+):$")
    fun uploadFile(
        fileName: String,
        httpPartName: String,
        httpMethod: String,
        urlOrPath: String,
        fileContent: String
    ) {
        makeMultiPartRequest(
            parts = listOf(httpPartName to ByteArrayBody(fileContent.toByteArray(), fileName)),
            httpMethod = httpMethod,
            urlOrPath = urlOrPath
        )
    }

    fun makeMultiPartRequest(
        httpMethod: String,
        urlOrPath: String,
        parts: Collection<Pair<String, ContentBody>>
    ) {
        val url = getFullUrl(urlOrPath)
        val request = getRequest(httpMethod, url)
        request.entity = MultipartEntityBuilder.create().apply {
            for ((partName, partBody) in parts) {
                addPart(partName, partBody)
            }
        }.build()
        val response = httpClient.execute(request, HttpResponseConverter.BYTE_ARRAY, commonHeaders)
        onResponse(url, HttpPost.METHOD_NAME, response)
    }

    @Given("^following default host name is set for HTTP requests$")
    fun setDefaultHostName(hostName: String) {
        defaultHostName.set(hostName)
    }

    private fun getFullUrl(urlOrPath: String): String {
        val url = if (urlOrPath.startsWith("http")) {
            urlOrPath
        } else {
            "http://$defaultHostName:${defaultPortProvider.port}$urlOrPath"
        }
        return (fixtureDataHelper.maybeExpandMetaValues(HttpClientTestFixture.TYPE, Unit, url) ?: url).toString()
    }

    private fun onResponse(url: String, method: String, response: HttpResponse<ByteArray>) {
        responses.getOrPut(method, CollectionInitializer.mutableList()) += ResponseEntry(url, response)
    }

    @Given("^HTTP ([^\\s]+) call to ([^\\s]+) is stubbed to return content '([^']+)'$")
    fun stubResponse(httpMethod: String, urlPattern: String, contentName: String) {
        val content = contentManager.getContent(contentName)
        httpClient.stubResponse(httpMethod, urlPattern.toRegex(), HttpResponse(
            status = HttpStatus.SC_OK,
            statusText = HttpStatus.SC_OK.toString(),
            body = content,
            headers = emptyMap()
        ))
    }

    @Then("^last HTTP ([^\\s]+) request returns the following:$")
    fun verifyLastSuccessfulResponse(httpMethod: String, expectedResponse: String) {
        val expected = fixtureDataHelper.prepareTestData(HttpClientTestFixture.TYPE, Unit, expectedResponse)
        assertThat(String(getLastResponse(httpMethod).body)).isEqualTo(expected)
    }

    @Then("^last HTTP ([^\\s]+) request finished by status code (\\d+)$")
    fun verifyLastResponseStatusCode(httpMethod: String, expectedStatus: Int) {
        val response = responses[httpMethod]?.last()?.response ?: fail(
            "no HTTP $httpMethod response is found"
        )
        assertThat(response.status).isEqualTo(expectedStatus)
    }

    @Then("^last HTTP ([^\\s]+) request returns the following JSON:$")
    fun verifyCompleteJsonResponse(httpMethod: String, expectedJson: String) {
        verifyJsonResponse(httpMethod, expectedJson, true)
    }

    @Then("^last HTTP ([^\\s]+) request returns JSON with at least the following data:$")
    fun verifyPartialJsonResponse(httpMethod: String, expectedJson: String) {
        verifyJsonResponse(httpMethod, expectedJson, false)
    }

    fun verifyJsonResponse(httpMethod: String, expectedJson: String, strict: Boolean) {
        val result = matchJsonResponse(httpMethod, expectedJson, strict)
        if (!result.success) {
            fail(result.failureValue)
        }
    }

    fun matchJsonResponse(
        httpMethod: String,
        expectedJson: String,
        strict: Boolean
    ): ProcessingResult<Unit, String> {
        val prepared = fixtureDataHelper.prepareTestData(
            type = HttpClientTestFixture.TYPE,
            context = Unit,
            data = CommonJsonUtil.prepareDynamicMarkers(expectedJson)
        ).toString()
        val expected = jsonApi.parseJson(prepared)
        val rawActual = String(getLastResponse(httpMethod).body)
        val actual = jsonApi.parseJson(rawActual)
        val result = CommonJsonUtil.compareAndBind(
            expected = expected,
            actual = actual,
            strict = strict
        )
        return if (result.errors.isEmpty()) {
            dynamicContext.storeBindings(result.boundDynamicValues)
            ProcessingResult.success()
        } else {
            ProcessingResult.failure(
                "found ${result.errors.size} error(s) on expected JSON content comparison" +
                result.errors.joinToString(prefix = "\n  *) ", separator = "\n  *) ") +
                "\ncomplete response:\n$rawActual"
            )
        }
    }

    @Then("^last HTTP ([^\\s]+) request returns JSON which does not have the following data:$")
    fun verifyNegativeJsonResponse(httpMethod: String, negativeJson: String) {
        val result = matchJsonResponse(httpMethod, negativeJson, false)
        if (result.success) {
            fail("""
                expected that last HTTP $httpMethod returns a JSON which doesn't have the following data:
                
                $negativeJson
                
                But this expectation fails. Full response:
                
                ${String(getLastResponse(httpMethod).body)}
            """.trimIndent())
        }
    }

    fun getLastResponse(httpMethod: String): HttpResponse<ByteArray> {
        return responses[httpMethod]?.last()?.response ?: fail(
            "no HTTP $httpMethod response is found"
        )
    }

    companion object {
        const val DEFAULT_HOST_NAME = "localhost"
    }

    private class ResponseEntry(
        val url: String,
        val response: HttpResponse<ByteArray>
    )
}