package tech.harmonysoft.oss.common.util

import java.math.BigDecimal

object ObjectUtil {

    /**
     * There are situations when two objects are different via [equals] but are the same via
     * [Comparable.compareTo] (e.g .[BigDecimal] with different scaling factor).
     *
     * That's why we provide a utility method which tries to compare arguments via [Comparable.compareTo]
     * if possible and falls back to [equals] otherwise.
     */
    @Suppress("UNCHECKED_CAST")
    fun areEqual(o1: Any?, o2: Any?): Boolean {
        if ((o1 == null && o2 == null) || o1 === o2) {
            return true
        }

        if (o1 == null || o2 == null || o1::class != o2::class) {
            return false
        }

        return if (o1 is Comparable<*>) {
            (o1 as Comparable<Any>).compareTo(o2 as Comparable<Any>) == 0
        } else {
            o1 == o2
        }
    }
}