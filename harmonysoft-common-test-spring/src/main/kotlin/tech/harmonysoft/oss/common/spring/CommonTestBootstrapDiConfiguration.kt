package tech.harmonysoft.oss.common.spring

import org.springframework.context.annotation.Configuration
import tech.harmonysoft.oss.common.host.HostInfo
import tech.harmonysoft.oss.test.fixture.meta.value.CommonFixtureBootstrapMetaValueMapper

@Configuration
open class CommonTestBootstrapDiConfiguration {

    open fun commonFixtureBootstrapMetaValueMapper(hostInfo: HostInfo): CommonFixtureBootstrapMetaValueMapper {
        return CommonFixtureBootstrapMetaValueMapper(hostInfo)
    }
}