package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.java.After
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.apache.hc.client5.http.classic.methods.*
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpHeaders
import org.apache.hc.core5.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import tech.harmonysoft.oss.common.collection.CollectionInitializer
import tech.harmonysoft.oss.http.client.TestHttpClient
import tech.harmonysoft.oss.http.client.cucumber.DefaultWebPortProvider
import tech.harmonysoft.oss.http.client.response.HttpResponse
import tech.harmonysoft.oss.http.client.response.HttpResponseConverter
import tech.harmonysoft.oss.json.JsonParser
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.content.TestContentManager
import tech.harmonysoft.oss.test.fixture.CommonTestFixture
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.json.CommonJsonUtil
import tech.harmonysoft.oss.test.util.TestUtil.fail
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class HttpStepDefinitions {

    private val responses = ConcurrentHashMap<String/* http method */, MutableList<ResponseEntry>>()
    private val defaultHostName = AtomicReference(DEFAULT_HOST_NAME)

    @Inject private lateinit var defaultPortProvider: DefaultWebPortProvider
    @Inject private lateinit var fixtureDataHelper: FixtureDataHelper
    @Inject private lateinit var httpClient: TestHttpClient
    @Inject private lateinit var contentManager: TestContentManager
    @Inject private lateinit var jsonParser: JsonParser
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
    }

    @Given("^HTTP ([^\\s]+) request to ([^\\s]+) is made$")
    fun makeRequest(httpMethod: String, urlOrPath: String) {
        val url = getFullUrl(urlOrPath)
        val request = getRequest(httpMethod, url)
        val response = httpClient.execute(request, HttpResponseConverter.BYTE_ARRAY)
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
        val headers = parseHeaders(rawHeaders)
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
        val headers = mapOf(HttpHeaders.CONTENT_TYPE to ContentType.APPLICATION_JSON.mimeType)
        val request = getRequest(httpMethod, url)
        val response = httpClient.execute(request, HttpResponseConverter.BYTE_ARRAY, headers)
        onResponse(url, httpMethod, response)
    }

    @Given("^HTTP ([^\\s]+) request to ([^\\s]+) is made with headers '([^']+)' and JSON body:$")
    fun makeRequestHeadersAndJsonBody(httpMethod: String, urlOrPath: String, headersString: String, json: String) {
        val url = getFullUrl(urlOrPath)
        val headers = mapOf(HttpHeaders.CONTENT_TYPE to ContentType.APPLICATION_JSON.mimeType) +
                      parseHeaders(headersString)
        val request = getRequest(httpMethod, url)
        val response = httpClient.execute(request, HttpResponseConverter.BYTE_ARRAY, headers)
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
        return fixtureDataHelper.maybeExpandMetaValues(CommonTestFixture.TYPE, Unit, url) ?: url
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
        assertThat(String(getLastResponse(httpMethod).body)).isEqualTo(expectedResponse)
    }

    @Then("^last HTTP ([^\\s]+) request finished by status code (\\d+)$")
    fun verifyLastResponseStatusCode(httpMethod: String, expectedStatus: Int) {
        val response = responses[httpMethod]?.last()?.response ?: fail(
            "no HTTP $httpMethod response is found"
        )
        assertThat(response.status).isEqualTo(expectedStatus)
    }

    @Then("^last HTTP ([^\\s]+) request returns the following JSON:$")
    fun verifyJsonResponse(httpMethod: String, expectedJson: String) {
        val prepared = fixtureDataHelper.prepareTestData(
            type = CommonTestFixture.TYPE,
            context = Any(),
            data = CommonJsonUtil.prepareDynamicMarkers(expectedJson)
        )
        val expected = parseJson(prepared)
        val rawActual = String(getLastResponse(httpMethod).body)
        val actual = parseJson(rawActual)
        val errors = CommonJsonUtil.compareAndBind(expected, actual, "<root>", dynamicContext)
        if (errors.isNotEmpty()) {
            fail("found ${errors.size} error(s) on expected JSON content comparison" +
                 errors.joinToString(prefix = "\n  *)", separator = "\n  *)"))
        }
    }

    private fun getLastResponse(httpMethod: String): HttpResponse<ByteArray> {
        return responses[httpMethod]?.last()?.response ?: fail(
            "no HTTP $httpMethod response is found"
        )
    }

    fun parseJson(json: String): Any {
        // it might be a regular json or json array, that's why we try to parse it as a map first and fallback to list
        return try {
            jsonParser.parse(json, Map::class)
        } catch (_: Exception) {
            try {
                jsonParser.parse(json, List::class)
            } catch (_: Exception) {
                fail("can't parse JSON from \n'$json'")
            }
        }
    }

    companion object {
        const val DEFAULT_HOST_NAME = "localhost"
    }

    private class ResponseEntry(
        val url: String,
        val response: HttpResponse<ByteArray>
    )
}