package tech.harmonysoft.oss.test.fixture

import kotlin.reflect.KClass
import tech.harmonysoft.oss.test.fixture.meta.value.FixtureMetaValueMapper

/**
 * We can use various placeholders in tests, e.g. quite often `<T>` stands for today, `<T - 1>` stands
 * for yesterday, etc. However, different domains might have different representation of the same placeholder.
 * For example, in some report `<T>` might be expanded to the current date without time; in 'store in db' use-case
 * the same `<T>` might need to reflect the time as well, etc.
 *
 * We identify target domain by the fixture type, so, if we can provide different [FixtureMetaValueMapper]
 * for 'report' and 'db' domains, they have different target [FixtureMetaValueMapper.type] values and
 * when we expand `<T>`, proper meta value mapper would be picked up depending on the target fixture type.
 */
data class FixtureType<T : Any>(
    val id: String,
    val fixtureContextClass: KClass<T>
) {
    override fun toString(): String {
        return id
    }
}