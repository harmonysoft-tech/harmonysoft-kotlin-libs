package tech.harmonysoft.oss.test.matcher

import tech.harmonysoft.oss.test.matcher.impl.SimpleMatcher
import tech.harmonysoft.oss.test.util.TestUtil
import javax.inject.Named

@Named
class MatchersFactory(
    factories: Collection<MatcherFactory>
) {

    private val byId = factories.associateBy { it.id }

    fun build(criteria: String): Matcher {
        return if (criteria == "<all>") {
            MatchAll
        } else {
            NON_STANDARD_CRITERIA_PATTERN.matchEntire(criteria)?.let { match ->
                byId[match.groupValues[1]]?.build(match.groupValues[2]) ?: TestUtil.fail(
                    "unknown match criteria '${match.groupValues[1]}', supported criteria: ${byId.keys.joinToString()}"
                )
            } ?: SimpleMatcher(criteria)
        }
    }

    object MatchAll : Matcher {

        override fun matches(input: String): Boolean {
            return true
        }

        override fun toString(): String {
            return "<match-all>"
        }
    }

    companion object {

        val NON_STANDARD_CRITERIA_PATTERN = """<([^:>]+):([^>]+)>""".toRegex()
    }
}