package tech.harmonysoft.oss.test.matcher.impl

import tech.harmonysoft.oss.test.matcher.Matcher

class SimpleMatcher(
    private val targetValue: String
) : Matcher {

    override fun matches(input: String): Boolean {
        return targetValue == input
    }

    override fun toString(): String {
        return "is '$targetValue'"
    }
}