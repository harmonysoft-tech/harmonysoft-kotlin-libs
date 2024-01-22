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
import tech.harmonysoft.oss.common.collection.CollectionUtil
import tech.harmonysoft.oss.common.collection.mapFirstNotNull
import tech.harmonysoft.oss.http.server.mock.config.MockHttpServerConfigProvider
import tech.harmonysoft.oss.http.server.mock.fixture.MockHttpServerPathTestFixture
import tech.harmonysoft.oss.http.server.mock.request.condition.DynamicRequestCondition
import tech.harmonysoft.oss.http.server.mock.request.condition.JsonBodyPathToMatcherCondition
import tech.harmonysoft.oss.http.server.mock.request.condition.ParameterName2ValueCondition
import tech.harmonysoft.oss.http.server.mock.response.ConditionalResponseProvider
import tech.harmonysoft.oss.http.server.mock.response.CountConstrainedResponseProvider
import tech.harmonysoft.oss.http.server.mock.response.DelayedResponseProvider
import tech.harmonysoft.oss.http.server.mock.response.ResponseProvider
import tech.harmonysoft.oss.jackson.JsonHelper
import tech.harmonysoft.oss.json.JsonApi
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.fixture.CommonTestFixture
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.json.CommonJsonUtil
import tech.harmonysoft.oss.test.manager.CommonTestManager
import tech.harmonysoft.oss.test.matcher.Matcher
import tech.harmonysoft.oss.test.util.NetworkUtil
import tech.harmonysoft.oss.test.util.TestUtil.fail
import tech.harmonysoft.oss.test.util.VerificationUtil

@Named
class MockHttpServerManager(
    private val configProvider: Optional<MockHttpServerConfigProvider>,
    private val jsonHelper: JsonHelper,
    private val fixtureDataHelper: FixtureDataHelper,
    private val jsonApi: JsonApi,
    private val dynamicContext: DynamicBindingContext,
    private val common: CommonTestManager,
    private val logger: Logger
) {

    private val mockRef = AtomicReference<ClientAndServer>()
    private val httpMock: ClientAndServer
        get() {
            startIfNecessary()
            return mockRef.get()
        }

    private val expectations = ConcurrentHashMap<HttpRequest, ExpectationInfo>()
    private val receivedRequests = CopyOnWriteArrayList<HttpRequest>()

    private val activeExpectationInfoRef = AtomicReference<ExpectationInfo?>()
    private val activeExpectationInfo: ExpectationInfo
        get() = activeExpectationInfoRef.get() ?: fail("no active mock HTTP request if defined")
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
    fun tearDown() {
        logger.info("Cleaning all mock HTTP server expectation rules")
        for (info in expectations.values) {
            httpMock.clear(info.expectationId)
        }
        expectations.clear()
        httpMock.clear(HttpRequest.request()) // clear all recorded requests
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
        httpMock.`when`(request).withId(info.expectationId).respond { req ->
            info.responseProviders.mapFirstNotNull { responseProvider ->
                responseProvider.maybeRespond(req)
            } ?: fail(
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
        val lastResponseProvider = lastResponseProviderRef.get() ?: fail(
            "can not configure the last HTTP response provider to act $count time(s) - no response provider "
            + "is defined so far"
        )
        val i = activeExpectationInfo.responseProviders.indexOfFirst { it === lastResponseProvider }
        if (i < 0) {
            fail(
                "something is very wrong - can not find the last response provider in the active expectation "
                + "info ($lastResponseProvider)"
            )
        }
        activeExpectationInfo.responseProviders[i] = CountConstrainedResponseProvider(lastResponseProvider, count)
    }

    fun delayLastResponse(delayMs: Long) {
        val lastResponseProvider = lastResponseProviderRef.get() ?: fail(
            "can not configure the last HTTP response provider to delay $delayMs ms - no response provider "
            + "is defined so far"
        )
        val i = activeExpectationInfo.responseProviders.indexOfFirst { it === lastResponseProvider }
        if (i < 0) {
            fail(
                "something is very wrong - can not find the last response provider in the active expectation "
                + "info ($lastResponseProvider)"
            )
        }
        activeExpectationInfo.responseProviders[i] = DelayedResponseProvider(lastResponseProvider, delayMs)
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
            val candidateBodies = httpMock.retrieveRecordedRequests(
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
                    ${it.second.size} error(s):
                    * ${it.second.joinToString("\n* ")}
                """.trimIndent()
                }
            )
        }
    }

    fun verifyRequestWithoutSpecificDataReceived(httpMethod: String, path: String, expectedRawJson: String) {
        val expandedPath = fixtureDataHelper.prepareTestData(MockHttpServerPathTestFixture.TYPE, Unit, path).toString()
        val prepared = fixtureDataHelper.prepareTestData(
            type = CommonTestFixture.TYPE,
            context = Any(),
            data = CommonJsonUtil.prepareDynamicMarkers(expectedRawJson)
        ).toString()
        val expected = jsonApi.parseJson(prepared)
        val findUnexpectedHttpCall = {
            val candidateBodies = httpMock.retrieveRecordedRequests(
                HttpRequest.request(expandedPath).withMethod(httpMethod)
            ).map { it.body.value as String }

            candidateBodies.find {
                val candidate = jsonApi.parseJson(it)
                val matches = findMatches(candidate, expected, "<root>")
                matches.isNotEmpty()
            }
        }
        if (common.expectTestVerificationFailure) {
            VerificationUtil.verifyConditionHappens(
                "unexpected HTTP $httpMethod call to $expandedPath didn't happen"
            ) {
                findUnexpectedHttpCall()?.let {
                    ProcessingResult.success()
                } ?: ProcessingResult.failure("no unexpected HTTP $httpMethod request to $path is found")
            }
        } else {
            VerificationUtil.verifyConditionDoesNotHappen(
                "unexpected HTTP $httpMethod request to $path is detected"
            ) {
                findUnexpectedHttpCall()?.let {
                    ProcessingResult.failure("found unexpected HTTP $httpMethod request to $path: $it")
                } ?: ProcessingResult.success()
            }
            val candidateBodies = httpMock.retrieveRecordedRequests(
                HttpRequest.request(expandedPath).withMethod(httpMethod)
            )
            if (candidateBodies.isEmpty()) {
                fail("no HTTP $httpMethod request to $path is made")
            }
        }
    }

    fun findMatches(actualData: Any?, toFind: Any?, path: String): Collection<String> {
        return when {
            toFind == "<any>" || toFind == actualData -> listOf("path $path is matched")
            actualData == null || toFind == null -> emptyList()

            toFind is Map<*, *> -> toFind.entries.fold(emptyList()) { acc, (key, value) ->
                val newMatches = if (actualData is Map<*, *>) {
                    key?.let { subKey ->
                        // we want to match 'null' value if it is really present in json
                        if (actualData.containsKey(subKey)) {
                            findMatches(actualData[subKey], value, "$path.$subKey")
                        } else {
                            emptyList()
                        }
                    } ?: emptyList()
                } else {
                    emptyList()
                }
                acc + newMatches
            }

            else -> {
                val toFindIterator = CollectionUtil.maybeGetIterator(toFind)
                if (toFindIterator == null) {
                    if (actualData == toFind) {
                        listOf("path $path is matched")
                    } else {
                        emptyList()
                    }
                } else if (CollectionUtil.maybeGetIterator(actualData) == null) {
                    emptyList()
                } else {
                    // we consider that there is a match if any element of the 'actual data' array/collection
                    //  matches any element of the 'to match' array/collection
                    mutableListOf<String>().apply {
                        for (subValueToFind in toFindIterator) {
                            this += findMatchInArray(actualData, subValueToFind, path)
                        }
                    }
                }
            }
        }
    }

    fun findMatchInArray(array: Any, toFind: Any?, path: String): Collection<String> {
        val iterator = CollectionUtil.maybeGetIterator(array) ?: return emptyList()
        var i = 0
        for (arrayElement in iterator) {
            val matches = findMatches(arrayElement, toFind, "$path[$i]")
            if (matches.isNotEmpty()) {
                return matches
            }
            i++
        }
        return emptyList()
    }

    fun verifyCallsCount(method: String, path: String, expectedCallsNumber: Int) {
        verifyCalls(
            method = method,
            path = path,
            conditionDescription = "exactly $expectedCallsNumber times(s)"
        ) { requests ->
            if (requests.size != expectedCallsNumber) {
                ProcessingResult.failure(
                    "expected that HTTP $method to path $path is done $expectedCallsNumber time(s) but it was done "
                    + "${requests.size} time(s)"
                )
            } else {
                ProcessingResult.success()
            }
        }
    }

    fun verifyMinCallsCount(method: String, path: String, expectedMinCallsNumber: Int) {
        verifyCalls(
            method = method,
            path = path,
            conditionDescription = "at least $expectedMinCallsNumber time(s)"
        ) { requests ->
            if (requests.size < expectedMinCallsNumber) {
                ProcessingResult.failure(
                    "expected that HTTP $method to path $path is done at least $expectedMinCallsNumber time(s) "
                    + "but it was done only ${requests.size} time(s)"
                )
            } else {
                ProcessingResult.success()
            }
        }
    }

    fun verifyCalls(method: String, path: String, conditionDescription: String, checker: (Array<out HttpRequest>) -> ProcessingResult<Unit, String>) {
        val expandedPath = fixtureDataHelper.prepareTestData(MockHttpServerPathTestFixture.TYPE, Unit, path).toString()
        val requests = httpMock.retrieveRecordedRequests(
            HttpRequest.request(expandedPath).withMethod(method)
        )
        VerificationUtil.verifyConditionHappens(
            description = "http $method request to $path is done $conditionDescription"
        ) {
            checker(requests)
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