package tech.harmonysoft.oss.test.fixture.meta.value

import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.test.fixture.FixtureType

/**
 * There might be cases when we use data like `<T>` (for today), `<temp-dir>`, `<free-port>`, etc in tests,
 * i.e. some meta values which should be expanded by test infrastructure in runtime
 *
 * This interface defines contract for a strategy which allows mapping such meta values in runtime
 */
interface FixtureMetaValueMapper<T : Any> {

    /**
     * We might use different meta-value expanding rules in different contexts, e.g. `<T>` (today) might be expanded
     * in one way with, say, database processing and different way for email body. That's why implementations of
     * this interface are qualified by fixture type.
     */
    val type: FixtureType<T>

    fun map(context: T, metaValue: String): ProcessingResult<String?, Unit>
}