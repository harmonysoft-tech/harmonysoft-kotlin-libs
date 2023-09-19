package tech.harmonysoft.oss.http.server.mock

import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Named
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.slf4j.Logger
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.common.collection.mapFirstNotNull
import tech.harmonysoft.oss.http.server.mock.config.MockHttpServerConfigProvider
import tech.harmonysoft.oss.http.server.mock.fixture.MockHttpServerPathTestFixture
import tech.harmonysoft.oss.http.server.mock.request.condition.DynamicRequestCondition
import tech.harmonysoft.oss.http.server.mock.request.condition.JsonBodyPathToMatcherCondition
import tech.harmonysoft.oss.http.server.mock.request.condition.ParameterName2ValueCondition
import tech.harmonysoft.oss.http.server.mock.response.ConditionalResponseProvider
import tech.harmonysoft.oss.http.server.mock.response.CountConstrainedResponseProvider
import tech.harmonysoft.oss.http.server.mock.response.ResponseProvider
import tech.harmonysoft.oss.jackson.JsonHelper
import tech.harmonysoft.oss.json.JsonApi
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.fixture.CommonTestFixture
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.json.CommonJsonUtil
import tech.harmonysoft.oss.test.matcher.Matcher
import tech.harmonysoft.oss.test.util.NetworkUtil
import tech.harmonysoft.oss.test.util.TestUtil
import tech.harmonysoft.oss.test.util.VerificationUtil

@Named
class MockHttpServerManager(
    private val configProvider: Optional<MockHttpServerConfigProvider>,
    private val jsonHelper: JsonHelper,
    private val fixtureDataHelper: FixtureDataHelper,
    private val jsonApi: JsonApi,
    private val dynamicContext: DynamicBindingContext,
    private val logger: Logger
) {

    private val mockRef = AtomicReference<ClientAndServer>()
    private val expectations = ConcurrentHashMap<HttpRequest, ExpectationInfo>()
    private val receivedRequests = CopyOnWriteArrayList<HttpRequest>()

    private val activeExpectationInfoRef = AtomicReference<ExpectationInfo?>()
    private val activeExpectationInfo: ExpectationInfo
        get() = activeExpectationInfoRef.get() ?: TestUtil.fail("no active mock HTTP request if defined")
    private val lastResponseProviderRef = AtomicReference<ResponseProvider?>()

    @BeforeEach
    fun setUp() {
        startIfNecessary()
    }

    fun startIfNecessary(): Int {
        val clientAndServer = mockRef.get()
        if (clientAndServer != null) {
            return clientAndServer.localPort
        }
        val port = if (configProvider.isPresent) {
            configProvider.get().data.port
        } else {
            NetworkUtil.freePort
        }
        logger.info("Starting mock http server on port {}", port)
        mockRef.set(ClientAndServer.startClientAndServer(port))
        return port
    }

    @AfterEach
    fun cleanExpectations() {
        logger.info("Cleaning all mock HTTP server expectation rules")
        for (info in expectations.values) {
            mockRef.get().clear(info.expectationId)
        }
        expectations.clear()
        logger.info("Finished cleaning all mock HTTP server expectation rules")
        receivedRequests.clear()
        activeExpectationInfoRef.set(null)
        lastResponseProviderRef.set(null)
    }

    fun targetRequest(request: HttpRequest) {
        expectations[request]?.let {
            activeExpectationInfoRef.set(it)
            return
        }
        val info = ExpectationInfo(request)
        mockRef.get().`when`(request).withId(info.expectationId).respond { req ->
            info.responseProviders.mapFirstNotNull { responseProvider ->
                responseProvider.maybeRespond(req)
            } ?: TestUtil.fail(
                "request $req is not matched by ${info.responseProviders.size} configured expectations:\n" +
                info.responseProviders.joinToString("\n")
            )
        }
        expectations[request] = info
        activeExpectationInfoRef.set(info)
    }

    fun setJsonRequestBodyCondition(path2matcher: Map<String, Matcher>) {
        addCondition(JsonBodyPathToMatcherCondition(path2matcher, jsonHelper))
    }

    fun setRequestParameterCondition(parameterName2value: Map<String, String>) {
        addCondition(ParameterName2ValueCondition(parameterName2value))
    }

    fun addCondition(condition: DynamicRequestCondition) {
        val current = activeExpectationInfo.dynamicRequestConditionRef.get()
        activeExpectationInfo.dynamicRequestConditionRef.set(current?.and(condition) ?: condition)
    }

    fun restrictLastResponseByCount(count: Int) {
        val lastResponseProvider = lastResponseProviderRef.get() ?: TestUtil.fail(
            "can not configure the last HTTP response provider to act $count time(s) - no response provider "
            + "is defined so far"
        )
        val i = activeExpectationInfo.responseProviders.indexOfFirst { it === lastResponseProvider }
        if (i < 0) {
            TestUtil.fail(
                "something is very wrong - can not find the last response provider in the active expectation "
                + "info ($lastResponseProvider)"
            )
        }
        activeExpectationInfo.responseProviders[i] = CountConstrainedResponseProvider(lastResponseProvider, count)
    }

    fun configureResponseWithCode(code: Int, response: String, headers: Map<String, String> = emptyMap()) {
        val condition = activeExpectationInfo.dynamicRequestConditionRef.getAndSet(null)
                        ?: DynamicRequestCondition.MATCH_ALL
        val parsedHeaders = headers.map { (key, value) ->
            Header(key, value)
        }
        val newResponseProvider = ConditionalResponseProvider(
            condition = condition,
            response = HttpResponse.response().withStatusCode(code).withBody(response).withHeaders(parsedHeaders)
        )
        // there is a possible case that we stub some default behavior in Background cucumber section
        // but want to define a specific behavior later on in Scenario. Then we need to replace
        // previous response provider by a new one. This code allows to do that
        activeExpectationInfo.responseProviders.removeIf {
            (it !is CountConstrainedResponseProvider
             && (it !is ConditionalResponseProvider || it.condition == condition)
            ).apply {
                if (this) {
                    logger.info(
                        "Replacing mock HTTP response provider for {}: {} -> {}",
                        activeExpectationInfo.request, it, newResponseProvider
                    )
                }
            }
        }

        val i = activeExpectationInfo.responseProviders.indexOfLast {
            it is CountConstrainedResponseProvider
        }
        if (i < 0) {
            // we add new provider as the first one in assumption that use-case for multiple providers is as below:
            //  * common generic stub is defined by default (e.g. in cucumber 'Background' section)
            //  * specific provider is defined in test
            // This way specific provider's condition would be tried first and generic provider would be called
            // only as a fallback
            activeExpectationInfo.responseProviders.add(0, newResponseProvider)
        } else {
            // we add the response provider after the last count-constrained provider
            activeExpectationInfo.responseProviders.add(i + 1, newResponseProvider)
        }

        logger.info(
            "{} HTTP response provider(s) are configured now: {}",
            activeExpectationInfo.responseProviders.size,
            activeExpectationInfo.responseProviders.joinToString()
        )

        lastResponseProviderRef.set(newResponseProvider)
    }

    fun verifyRequestReceived(httpMethod: String, path: String, expectedRawJson: String) {
        val expandedPath = fixtureDataHelper.prepareTestData(MockHttpServerPathTestFixture.TYPE, Unit, path).toString()
        val prepared = fixtureDataHelper.prepareTestData(
            type = CommonTestFixture.TYPE,
            context = Any(),
            data = CommonJsonUtil.prepareDynamicMarkers(expectedRawJson)
        ).toString()
        val expected = jsonApi.parseJson(prepared)

        VerificationUtil.verifyConditionHappens {
            val candidateBodies = mockRef.get().retrieveRecordedRequests(
                HttpRequest.request(expandedPath).withMethod(httpMethod)
            ).map { it.body.value as String }

            val bodiesWithErrors = candidateBodies.map { candidateBody ->
                val candidate = jsonApi.parseJson(candidateBody)
                val result = CommonJsonUtil.compareAndBind(
                    expected = expected,
                    actual = candidate,
                    strict = false
                )
                if (result.errors.isEmpty()) {
                    dynamicContext.storeBindings(result.boundDynamicValues)
                    return@verifyConditionHappens ProcessingResult.success()
                }
                candidateBody to result.errors
            }
            ProcessingResult.failure(
                "can't find HTTP $httpMethod request to path $expandedPath with at least the following JSON body:" +
                "\n$expectedRawJson" +
                "\n\n${candidateBodies.size} request(s) with the same method and path are found:\n"
                + bodiesWithErrors.joinToString("\n-------------------------------------------------\n") {
                    """
                    ${it.first}
                    ${it.second} error(s):
                    * ${it.second.joinToString("\n* ")}
                """.trimIndent()
                }
            )
        }
    }

    fun verifyNoCallIsMade(method: String, path: String) {
        val expandedPath = fixtureDataHelper.prepareTestData(MockHttpServerPathTestFixture.TYPE, Unit, path).toString()
        val requests = mockRef.get().retrieveRecordedRequests(
            HttpRequest.request(expandedPath).withMethod(method)
        )
        if (requests.isNotEmpty()) {
            TestUtil.fail(
                "expected that no HTTP $method requests to $path are done but there were "
                + "${requests.size} request(s): \n"
                + requests.joinToString("\n---------------\n")
            )
        }
    }

    class ExpectationInfo(
        val request: HttpRequest
    ) {

        val expectationId = UUID.randomUUID().toString()
        val responseProviders = CopyOnWriteArrayList<ResponseProvider>()
        val dynamicRequestConditionRef = AtomicReference<DynamicRequestCondition?>()
    }

}