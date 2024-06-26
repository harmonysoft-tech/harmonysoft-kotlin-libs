package tech.harmonysoft.oss.test.fixture.meta.function

import jakarta.inject.Named

@Named
class FloatMetaFunction : FixtureMetaFunction {

    override val functionName = "float"

    override fun applyFunction(value: String): Any {
        return value.toFloat()
    }
}