package tech.harmonysoft.oss.common.di

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tech.harmonysoft.oss.common.host.HostInfo
import tech.harmonysoft.oss.common.string.util.isNullOrBlankEffective
import java.net.InetAddress

@Configuration
open class BootstrapCommonConfiguration {

    @Bean
    open fun hostInfo(): HostInfo {
        val inetAddress = InetAddress.getLocalHost()
        val hostName = inetAddress.canonicalHostName
        if (hostName.isNullOrBlankEffective()) {
            throw IllegalStateException("failed to derive host name from current environment")
        }
        return HostInfo(hostName)
    }
}