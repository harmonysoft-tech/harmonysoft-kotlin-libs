package tech.harmonysoft.oss.test.fixture.meta.function

import jakarta.inject.Named

@Named
class DoubleMetaFunction : FixtureMetaFunction {

    override val functionName = "double"

    override fun applyFunction(value: String): Any {
        return value.toDouble()
    }
}