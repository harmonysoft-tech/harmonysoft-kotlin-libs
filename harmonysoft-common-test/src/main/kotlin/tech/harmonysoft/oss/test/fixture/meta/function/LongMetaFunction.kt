package tech.harmonysoft.oss.test.fixture.meta.function

import jakarta.inject.Named

@Named
class LongMetaFunction : FixtureMetaFunction {

    override val functionName = "long"

    override fun applyFunction(value: String): Any {
        return value.toLong()
    }
}