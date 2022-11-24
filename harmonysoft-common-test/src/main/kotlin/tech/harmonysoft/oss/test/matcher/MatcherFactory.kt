package tech.harmonysoft.oss.test.matcher

interface MatcherFactory {

    val id: String

    fun build(criteria: String): Matcher
}