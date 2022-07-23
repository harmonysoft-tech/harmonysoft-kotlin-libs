package tech.harmonysoft.oss.common.type

import tech.harmonysoft.oss.common.number.NumberUtil
import javax.inject.Named

@Named
class DefaultDoubleTypeManager : TypeManager<Double> {

    override val targetType = Double::class

    override val targetContext = TypeManagerContext.DEFAULT

    override fun maybeParse(rawValue: String): Double? {
        return rawValue.trim().takeIf(String::isNotEmpty)?.toDouble()
    }

    override fun compareTo(first: Double, second: Double?): Int? {
        return second?.let {
            (first * NumberUtil.FLOATING_POINT_SCALE).toLong().compareTo(
                (it * NumberUtil.FLOATING_POINT_SCALE).toLong()
            )
        }
    }
}