package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.datatable.DataTable
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import javax.inject.Inject
import org.mockserver.model.HttpRequest
import tech.harmonysoft.oss.http.server.mock.MockHttpServerManager
import tech.harmonysoft.oss.http.server.mock.fixture.MockHttpServerPathTestFixture
import tech.harmonysoft.oss.http.server.mock.request.ExpectedRequestConfigurer
import tech.harmonysoft.oss.http.server.mock.request.condition.PartialJsonMatchCondition
import tech.harmonysoft.oss.json.JsonApi
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.json.CommonJsonUtil
import tech.harmonysoft.oss.test.matcher.MatchersFactory
import tech.harmonysoft.oss.test.util.TestUtil.fail

class MockHttpServerStepDefinitions {

    @Inject private lateinit var manager: MockHttpServerManager

    @Inject private lateinit var requestConfigurers: Collection<ExpectedRequestConfigurer>
    @Inject private lateinit var jsonApi: JsonApi
    @Inject private lateinit var fixtureDataHelper: FixtureDataHelper
    @Inject private lateinit var matchersFactory: MatchersFactory
    @Inject private lateinit var dynamicContext: DynamicBindingContext

    @Before
    fun startIfNecessary() {
        manager.startIfNecessary()
    }

    @After
    fun cleanExpectations() {
        manager.cleanExpectations()
    }

    @Given("^the following HTTP request is received by mock server:$")
    fun targetRequest(data: DataTable) {
        val rows = data.asLists()
        if (rows.size != 2) {
            fail(
                "unexpected mock http request match format, expected exactly two rows - criteria name " +
                "and criteria values but got ${rows.size}: $rows"
            )
        }
        val index2configurer = rows.first().mapIndexed { i, criteriaName ->
            requestConfigurers.find { criteriaName == it.criteriaName }?.let {
                i to it
            } ?: fail(
                "unknown mock http criteria '$criteriaName', available: ${
                    requestConfigurers.joinToString {
                        it.criteriaName
                    }
                }"
            )
        }.toMap()
        val request = rows.last().foldIndexed(HttpRequest.request()) { i, request, criteriaValue ->
            index2configurer[i]?.configure(request, criteriaValue) ?: fail("I can't happen")
        }
        manager.targetRequest(request)
    }

    @Given("^mock HTTP request body is a JSON with the following values:$")
    fun setJsonRequestBodyCondition(data: DataTable) {
        val criteria = data.asMaps().first().mapValues {
            matchersFactory.build(it.value)
        }
        manager.setJsonRequestBodyCondition(criteria)
    }

    @Given("^mock HTTP request has the following query parameters?:$")
    fun setRequestParameterCondition(data: DataTable) {
        val criteria = data.asMaps().first()
        manager.setRequestParameterCondition(criteria)
    }

    @Given("^mock HTTP request body is a JSON with at least the following data:$")
    fun setPartialJsonMatchCondition(rawExpected: String) {
        val prepared = fixtureDataHelper.prepareTestData(
            type = MockHttpServerPathTestFixture.TYPE,
            context = Unit,
            data = CommonJsonUtil.prepareDynamicMarkers(rawExpected)
        ).toString()
        val parsedExpected = jsonApi.parseJson(prepared)
        manager.addCondition(PartialJsonMatchCondition(rawExpected, parsedExpected, jsonApi, dynamicContext))
    }

    @Given("^the following mock HTTP response is returned:$")
    fun configureResponse(response: String) {
        configureResponseWithCode(200, response)
    }

    @Given("^the following mock HTTP response with code (\\d+) is returned:$")
    fun configureResponseWithCode(code: Int, response: String) {
        manager.configureResponseWithCode(code, response)
    }

    @Given("^the last mock HTTP response is provided (\\d+) times?$")
    fun restrictLastResponseByCount(count: Int) {
        manager.restrictLastResponseByCount(count)
    }

    @Given("^the last mock HTTP response is delayed by (\\d+) ms$")
    fun delayLastResponse(delayMs: Long) {
        manager.delayLastResponse(delayMs)
    }

    @Then("^the following ([^\\s]+) request for path ([^\\s]+) with at least this JSON data is received by mock HTTP server:$")
    fun verifyRequestReceived(httpMethod: String, path: String, expectedRawJson: String) {
        manager.verifyRequestReceived(httpMethod, path, expectedRawJson)
    }

    @Then("^no HTTP ([^\\s]+) call to ([^\\s]+) is made$")
    fun verifyNoCallIsMade(method: String, path: String) {
        manager.verifyNoCallIsMade(method, path)
    }
}