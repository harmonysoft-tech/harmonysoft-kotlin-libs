package tech.harmonysoft.oss.test.input

import jakarta.inject.Named
import tech.harmonysoft.oss.test.binding.DynamicBindingKey
import tech.harmonysoft.oss.test.binding.DynamicBindingUtil
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.fixture.FixtureType

@Named
class CommonTestInputHelper(
    private val fixtureHelper: FixtureDataHelper
) {

    fun <T : Any> parse(
        fixtureType: FixtureType<T>,
        fixtureContext: T,
        input: Map<String, String>
    ): TestInputRecord {
        return parse(
            fixtureType = fixtureType,
            fixtureContext = fixtureContext,
            input = listOf(input)
        ).first()
    }

    fun <T : Any> parse(
        fixtureType: FixtureType<T>,
        fixtureContext: T,
        input: List<Map<String, String>>
    ): List<TestInputRecord> {
        return input.map { rawRow ->
            val data = mutableMapOf<String, String>()
            val toBind = mutableMapOf<String, DynamicBindingKey>()
            for ((column, value) in rawRow) {
                DynamicBindingUtil.TO_BIND_REGEX.matchEntire(column)?.let {
                    toBind[it.groupValues[1]] = DynamicBindingKey(value)
                } ?: run {
                    data[column] = value
                }
            }
            TestInputRecord(
                data = fixtureHelper.prepareTestData(fixtureType, fixtureContext, data),
                toBind = toBind
            )
        }
    }
}