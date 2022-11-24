package tech.harmonysoft.oss.test.matcher

fun interface Matcher {

    fun matches(input: String): Boolean
}