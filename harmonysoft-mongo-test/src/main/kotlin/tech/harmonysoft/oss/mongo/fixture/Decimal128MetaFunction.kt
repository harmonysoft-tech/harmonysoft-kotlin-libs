package tech.harmonysoft.oss.mongo.fixture

import javax.inject.Named
import org.bson.types.Decimal128
import tech.harmonysoft.oss.test.fixture.meta.function.FixtureMetaFunction

@Named
class Decimal128MetaFunction : FixtureMetaFunction {

    override val functionName = "decimal128"

    override fun applyFunction(value: String): Any {
        return Decimal128.parse(value)
    }
}