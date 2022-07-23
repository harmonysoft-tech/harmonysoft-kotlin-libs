@file:OptIn(ExperimentalContracts::class)
@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package tech.harmonysoft.oss.common.string.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Supersedes built-in Kotlin extension function because it uses built-in [isBlank] function which, in turn,
 * creates [IntRange] object for every call. If such calls happen often, then quite a few of unnecessary objects
 * are created.
 *
 * That's why we have our own alternative which doesn't use additional memory
 */
fun CharSequence?.isNullOrBlankEffective(): Boolean {
    contract {
        returns(false) implies (this@isNullOrBlankEffective != null)
    }
    return this == null || isBlankEffective()
}

/**
 * Supersedes built-in Kotlin extension function because it creates [IntRange] object for every call.
 * If such calls happen often, then quite a few of unnecessary objects are created.
 *
 * That's why we have our own alternative which doesn't use additional memory
 */
fun CharSequence.isBlankEffective(): Boolean {
    var i = -1
    val max = length
    while (++i < max) {
        val c = get(i)
        if (!c.isWhitespace()) {
            return false
        }
    }
    return true
}

/**
 * Supersedes built-in Kotlin extension function because it uses built-in [isBlank] function which, in turn,
 * creates [IntRange] object for every call. If such calls happen often, then quite a few of unnecessary objects
 * are created.
 *
 * That's why we have our own alternative which doesn't use additional memory
 */
fun CharSequence.isNotBlankEffective(): Boolean {
    return !isBlankEffective()
}

fun CharSequence?.isNotNullNotBlankEffective(): Boolean {
    contract {
        returns(true) implies (this@isNotNullNotBlankEffective != null)
    }
    return !isNullOrBlankEffective()
}

object StringUtil {

    const val EMPTY_STRING = ""
    val LINE_FEED_REGEX = """\s*\r?\n\s*""".toRegex()

    /**
     * Normalizes given string and returns it represented as a single line:
     *
     * ```
     * toSingleLine("""
     *     select a, b
     *     from c
     *     where d = 1
     *       and e = 2
     * """) == "select a, b from c where d = 1 and e = 2"
     * ```
     */
    fun toSingleLine(s: String): String {
        return s.trimIndent().trim().replace(LINE_FEED_REGEX, " ")
    }
}