package tech.harmonysoft.oss.common.info.impl

import tech.harmonysoft.oss.common.info.CommonInfoKey
import tech.harmonysoft.oss.common.info.CommonInfoProvider
import tech.harmonysoft.oss.common.time.clock.ClockProvider
import tech.harmonysoft.oss.common.time.util.DateTimeHelper
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Named

@Named
class StartTimeInfoProvider(
    clockProvider: ClockProvider,
    dateTimeHelper: DateTimeHelper
) : CommonInfoProvider {

    val startTimeMs = clockProvider.data.millis()

    val startTimeString = dateTimeHelper.formatDateTime(ZonedDateTime.ofInstant(
        Instant.ofEpochMilli(startTimeMs), clockProvider.data.zone
    ).toLocalDateTime())

    override val info = mapOf(CommonInfoKey.START_TIME to startTimeString)
}