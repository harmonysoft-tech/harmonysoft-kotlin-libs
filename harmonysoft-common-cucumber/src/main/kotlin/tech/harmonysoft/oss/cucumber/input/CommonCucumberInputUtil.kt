package tech.harmonysoft.oss.cucumber.input

import io.cucumber.datatable.DataTable
import tech.harmonysoft.oss.test.binding.DynamicBindingKey
import tech.harmonysoft.oss.test.binding.DynamicBindingUtil

object CommonCucumberInputUtil {

    fun parse(dataTable: DataTable): List<CucumberRecord> {
        return dataTable.asMaps().map { rawRow ->
            val data = mutableMapOf<String, String>()
            val toBind = mutableMapOf<String, DynamicBindingKey>()
            for ((column, value) in rawRow) {
                DynamicBindingUtil.TO_BIND_REGEX.matchEntire(column)?.let {
                    toBind[it.groupValues[1]] = DynamicBindingKey("${it.groupValues[1]}:$value")
                } ?: run {
                    data[column] = value
                }
            }
            CucumberRecord(
                data = data,
                toBind = toBind
            )
        }
    }
}