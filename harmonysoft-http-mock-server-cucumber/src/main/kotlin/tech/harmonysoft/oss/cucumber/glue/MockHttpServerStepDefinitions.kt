package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.datatable.DataTable
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.slf4j.Logger
import tech.harmonysoft.oss.common.collection.mapFirstNotNull
import tech.harmonysoft.oss.http.server.mock.request.ExpectedRequestConfigurer
import tech.harmonysoft.oss.http.server.mock.request.condition.DynamicRequestCondition
import tech.harmonysoft.oss.http.server.mock.request.condition.JsonBodyPathToMatcherCondition
import tech.harmonysoft.oss.http.server.mock.request.condition.ParameterName2ValueCondition
import tech.harmonysoft.oss.http.server.mock.response.ConditionalResponseProvider
import tech.harmonysoft.oss.http.server.mock.response.ResponseProvider
import tech.harmonysoft.oss.jackson.JsonHelper
import tech.harmonysoft.oss.test.matcher.Matcher
import tech.harmonysoft.oss.test.matcher.MatchersFactory
import tech.harmonysoft.oss.test.util.NetworkUtil
import tech.harmonysoft.oss.test.util.TestUtil
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class MockHttpServerStepDefinitions {

    private val expectations = ConcurrentHashMap<HttpRequest, ExpectationInfo>()
    private val activeExpectationInfoRef = AtomicReference<ExpectationInfo?>()
    private val activeExpectationInfo: ExpectationInfo
        get() = activeExpectationInfoRef.get() ?: TestUtil.fail("no active mock HTTP request if defined")

    @Inject private lateinit var configProvider: Optional<MockHttpServerConfigProvider>
    @Inject private lateinit var requestConfigurers: Collection<ExpectedRequestConfigurer>
    @Inject private lateinit var jsonHelper: JsonHelper
    @Inject private lateinit var matchersFactory: MatchersFactory
    @Inject private lateinit var logger: Logger

    @Before
    fun startIfNecessary() {
        if (mockRef.get() != null) {
            return
        }
        val port = if (configProvider.isPresent) {
            configProvider.get().data.port
        } else {
            NetworkUtil.freePort
        }
        logger.info("Starting mock http server on port {}", port)
        mockRef.set(ClientAndServer.startClientAndServer(port))
    }

    @After
    fun cleanExpectations() {
        logger.info("Cleaning all mock HTTP server expectation rules")
        for (info in expectations.values) {
            mockRef.get().clear(info.expectationId)
        }
        logger.info("Finished cleaning all mock HTTP server expectation rules")
    }

    @Given("^the following HTTP request is received by mock server:$")
    fun targetRequest(data: DataTable) {
        val rows = data.asLists()
        if (rows.size != 2) {
            TestUtil.fail(
                "unexpected mock http request match format, expected exactly two rows - criteria name " +
                "and criteria values but got ${rows.size}: $rows"
            )
        }
        val index2configurer = rows.first().mapIndexed { i, criteriaName ->
            requestConfigurers.find { criteriaName == it.criteriaName }?.let {
                i to it
            } ?: TestUtil.fail(
                "unknown mock http criteria '$criteriaName', available: ${
                    requestConfigurers.joinToString {
                        it.criteriaName
                    }
                }"
            )
        }.toMap()
        val request = rows.last().foldIndexed(HttpRequest.request()) { i, request, criteriaValue ->
            index2configurer[i]?.configure(request, criteriaValue) ?: TestUtil.fail("I can't happen")
        }
        targetRequest(request)
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

    @Given("^mock HTTP request body is a JSON with the following values:$")
    fun setJsonRequestBodyCondition(data: DataTable) {
        val criteria = data.asMaps().first().mapValues {
            matchersFactory.build(it.value)
        }
        setJsonRequestBodyCondition(criteria)
    }

    fun setJsonRequestBodyCondition(path2matcher: Map<String, Matcher>) {
        addCondition(JsonBodyPathToMatcherCondition(path2matcher, jsonHelper))
    }

    @Given("^mock HTTP request has the following query parameters?:$")
    fun setRequestParameterCondition(data: DataTable) {
        val criteria = data.asMaps().first()
        setRequestParameterCondition(criteria)
    }

    fun setRequestParameterCondition(parameterName2value: Map<String, String>) {
        addCondition(ParameterName2ValueCondition(parameterName2value))
    }

    fun addCondition(condition: DynamicRequestCondition) {
        val current = activeExpectationInfo.dynamicRequestConditionRef.get()
        // there is a possible case that we stub common condition-based call by default and later
        // on overwrite it in tests. We want to drop the common one to replace by the given one then
        activeExpectationInfo.responseProviders.removeIf {
            (it is ConditionalResponseProvider && it.condition == condition).apply {
                logger.info("Dropping mock HTTP response provider {} because it's being replaced by a new one", it)
            }
        }
        activeExpectationInfo.dynamicRequestConditionRef.set(current?.and(condition) ?: condition)
    }

    @Given("^the following mock HTTP response is returned:$")
    fun configureResponse(response: String) {
        configureResponseWithCode(200, response)
    }

    @Given("^the following mock HTTP response with code (\\d+) is returned:$")
    fun configureResponseWithCode(code: Int, response: String) {
        val condition = activeExpectationInfo.dynamicRequestConditionRef.getAndSet(null)
                        ?: DynamicRequestCondition.MATCH_ALL
        val newResponseProvider = ConditionalResponseProvider(
            condition = condition,
            response = HttpResponse.response().withStatusCode(code).withBody(response)
        )
        // there is a possible case that we stub some default behavior in Background cucumber section
        // but want to define a specific behavior later on in Scenario. Then we need to replace
        // previous response provider by a new one. This code allows to do that
        activeExpectationInfo.responseProviders.removeIf {
            (it !is ConditionalResponseProvider || it.condition == condition).apply {
                if (this) {
                    logger.info(
                        "Replacing mock HTTP response provider for {}: {} -> {}",
                        activeExpectationInfo.request, it, newResponseProvider
                    )
                }
            }
        }
        activeExpectationInfo.responseProviders += newResponseProvider
    }

    class ExpectationInfo(
        val request: HttpRequest
    ) {

        val expectationId = UUID.randomUUID().toString()
        val responseProviders = CopyOnWriteArrayList<ResponseProvider>()
        val dynamicRequestConditionRef = AtomicReference<DynamicRequestCondition?>()
    }

    companion object {

        private val mockRef = AtomicReference<ClientAndServer>()
    }
}