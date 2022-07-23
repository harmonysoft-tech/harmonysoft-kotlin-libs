package tech.harmonysoft.oss.common.host

import tech.harmonysoft.oss.common.execution.CommonContextKey
import tech.harmonysoft.oss.common.info.CommonInfoProvider

data class HostInfo(
    val hostName: String
) : CommonInfoProvider {

    val shortHostName = run {
        val i = hostName.indexOf('.')
        if (i > 0) {
            hostName.substring(0, i)
        } else {
            hostName
        }
    }

    override val info = mapOf(CommonContextKey.HOST to hostName)
}