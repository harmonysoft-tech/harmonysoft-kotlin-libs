package tech.harmonysoft.oss.cucumber.input

import tech.harmonysoft.oss.test.binding.DynamicBindingKey

/**
 * We support test infrastructure when dynamic values generated during test execution are picked up in runtime
 * and are reused later. For example, cucumber steps might create a db record, bind it's auto-generated id
 * and us it later on:
 *
 * ```
 * Given the following record is inserted into table my-table of db my-db:
 *   | COLUMN1 | COLUMN2 | <bind:ID> |
 *   | value1  | value2  | id        |
 *
 * Then HTTP GET request to /order/<bound:ID> returns the following content:
 *   """
 *   { "success": true }
 *   """
 * ```
 *
 * This class represents a single cucumber record with separated 'known values' and 'values to bind'.
 */
data class CucumberRecord(
    val data: Map<String, String>,
    val toBind: Map<String, DynamicBindingKey>
)