package tech.harmonysoft.oss.common.data

interface EqualityStrategy {

    fun <T> areEqual(first: T?, second: T?): Boolean
}