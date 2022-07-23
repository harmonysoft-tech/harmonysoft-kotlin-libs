package tech.harmonysoft.oss.test.fixture

/**
 * There are possible cases when we prepare test data for some system and it's necessary to provide
 * values for required columns but the actual data doesn't have them. For example, a db table might
 * have required columns but we might not want to explicitly define them when inserting test records.
 *
 * It's convenient to provide an enricher for the target domain to fill such required missing values then.
 *
 * This interface defines a contract for such enrichment strategy.
 */
interface FixtureDataEnricher<T : Any> {

    /**
     * We might use different enriching rules in different contexts. That's why implementations of
     * this interface are qualified by fixture type.
     */
    val type: FixtureType<T>

    fun enrich(context: T, data: Map<String, String?>): Map<String, String?>
}