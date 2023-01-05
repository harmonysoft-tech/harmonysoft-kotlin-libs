package tech.harmonysoft.oss.test.matcher.impl

import tech.harmonysoft.oss.test.matcher.Matcher
import tech.harmonysoft.oss.test.matcher.MatcherFactory
import javax.inject.Named

@Named
class HasTextMatcherFactory : MatcherFactory {

    override val id = "has-text"

    override fun build(criteria: String): Matcher {
        return Impl(criteria)
    }

    data class Impl(
        private val targetText: String
    ) : Matcher {

        override fun matches(input: String): Boolean {
            return input.contains(targetText)
        }

        override fun toString(): String {
            return "has text '$targetText'"
        }
    }
}