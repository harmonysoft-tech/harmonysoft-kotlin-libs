package tech.harmonysoft.oss.cucumber.input

import io.cucumber.datatable.DataTable
import tech.harmonysoft.oss.test.binding.DynamicBindingKey
import tech.harmonysoft.oss.test.binding.DynamicBindingUtil
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.fixture.FixtureType
import javax.inject.Named

@Named
class CommonCucumberInputHelper(
    private val fixtureHelper: FixtureDataHelper
) {

    fun <T : Any> parse(fixtureType: FixtureType<T>, fixtureContext: T, dataTable: DataTable): List<CucumberRecord> {
        return dataTable.asMaps().map { rawRow ->
            val data = mutableMapOf<String, String>()
            val toBind = mutableMapOf<String, DynamicBindingKey>()
            for ((column, value) in rawRow) {
                DynamicBindingUtil.TO_BIND_REGEX.matchEntire(column)?.let {
                    toBind[it.groupValues[1]] = DynamicBindingKey(value)
                } ?: run {
                    data[column] = value
                }
            }
            CucumberRecord(
                data = fixtureHelper.prepareTestData(fixtureType, fixtureContext, data),
                toBind = toBind
            )
        }
    }
}