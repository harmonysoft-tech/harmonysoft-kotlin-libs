package tech.harmonysoft.oss.test.fixture.meta.function

import javax.inject.Named

@Named
class IntMetaFunction : FixtureMetaFunction {

    override val functionName = "int"

    override fun applyFunction(value: String): Any {
        return value.toInt()
    }
}