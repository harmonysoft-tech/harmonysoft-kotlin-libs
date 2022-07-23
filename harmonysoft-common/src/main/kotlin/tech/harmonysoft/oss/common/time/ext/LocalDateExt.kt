package tech.harmonysoft.oss.common.time.ext

import java.time.LocalDate

/**
 * Allows to create [LocalDate] range:
 *
 * ```
 * val d1: LocalDate = ...
 * val d2: LocalDate = ...
 * for (date in d1..d2) {
 *     // process date
 * }
 * ```
 */
operator fun LocalDate.rangeTo(otherInclusive: LocalDate) = LocalDateProgression(this, otherInclusive)

data class LocalDateProgression(
    override val start: LocalDate,
    override val endInclusive: LocalDate,
    val stepDays: Long = 1L
) : Iterable<LocalDate>, ClosedRange<LocalDate> {

    init {
        if (endInclusive < start) {
            throw IllegalArgumentException(
                "end date ($endInclusive) must be not before the start date ($start)"
            )
        }
    }

    override fun iterator(): Iterator<LocalDate> {
        return LocalDateIterator(start, endInclusive, stepDays)
    }

    infix fun step(days: Long) = LocalDateProgression(start, endInclusive, days)
}

data class LocalDateIterator(
    private val start: LocalDate,
    private val endInclusive: LocalDate,
    private val stepDays: Long = 1L
) : Iterator<LocalDate> {

    private var current = start

    init {
        if (endInclusive < start) {
            throw IllegalArgumentException(
                "end date ($endInclusive) must be not before the start date ($start)"
            )
        }
    }

    override fun hasNext(): Boolean {
        return current <= endInclusive
    }

    override fun next(): LocalDate {
        return current.apply {
            current = current.plusDays(stepDays)
        }
    }
}