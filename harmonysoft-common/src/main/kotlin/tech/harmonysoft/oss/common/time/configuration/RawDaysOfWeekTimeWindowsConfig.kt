package tech.harmonysoft.oss.common.time.configuration

import tech.harmonysoft.oss.common.time.schedule.impl.DaysOfWeekTimeWindows
import tech.harmonysoft.oss.common.time.schedule.impl.TimeWindow
import java.time.DayOfWeek

data class RawDaysOfWeekTimeWindowsConfig(
    val daysOfWeek: Collection<DayOfWeek>,
    val windows: Collection<RawTimeWindowConfig>?
) {

    fun toDaysOfWeekTimeWindows(): DaysOfWeekTimeWindows {
        return DaysOfWeekTimeWindows(
            daysOfWeek = daysOfWeek.toSet(),
            _timeWindows = windows?.map { TimeWindow.parse(it.window) }?.toSet() ?: emptySet()
        )
    }
}