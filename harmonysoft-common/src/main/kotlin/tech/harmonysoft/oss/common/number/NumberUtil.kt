package tech.harmonysoft.oss.common.number

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow
import kotlin.math.roundToLong

object NumberUtil {

    object NormalizedBigDecimal {
        val ZERO = normalize(BigDecimal.ZERO)
        val ONE = normalize(BigDecimal.ONE)
        val MINUS_ONE = normalize(BigDecimal.valueOf(-1L))
        val TEN = normalize(BigDecimal.TEN)
    }

    val FLOATING_POINT_SCALE = System.getProperty("harmonysoft.number.floating.scale")?.toInt() ?: 6

    @Suppress("UNCHECKED_CAST")
    fun <T> normalize(value: T): T {
        return if (value is BigDecimal) {
            value.setScale(FLOATING_POINT_SCALE, RoundingMode.HALF_UP) as T
        } else {
            value
        }
    }

    fun round(value: Double, precision: Int): Double {
        val multiplier = 10.0.pow(precision.toDouble())
        return (value * multiplier).roundToLong() / multiplier
    }
}