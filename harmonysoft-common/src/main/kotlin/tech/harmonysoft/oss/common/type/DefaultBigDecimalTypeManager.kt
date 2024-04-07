package tech.harmonysoft.oss.common.type

import tech.harmonysoft.oss.common.number.NumberUtil
import java.math.BigDecimal
import jakarta.inject.Named

@Named
class DefaultBigDecimalTypeManager : TypeManager<BigDecimal> {

    override val targetType = BigDecimal::class

    override val targetContext = TypeManagerContext.DEFAULT

    override fun maybeParse(rawValue: String): BigDecimal? {
        return rawValue.trim().takeIf(String::isNotEmpty)?.let {
            NumberUtil.normalize(BigDecimal(it))
        }
    }
}