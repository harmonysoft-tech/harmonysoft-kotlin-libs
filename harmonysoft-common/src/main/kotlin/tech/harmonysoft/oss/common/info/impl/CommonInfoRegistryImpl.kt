package tech.harmonysoft.oss.common.info.impl

import tech.harmonysoft.oss.common.info.CommonInfoProvider
import tech.harmonysoft.oss.common.info.CommonInfoRegistry
import javax.inject.Named

@Named
class CommonInfoRegistryImpl(
    private val providers: Collection<CommonInfoProvider>
) : CommonInfoRegistry {

    override val info: Map<String, String>
        get() = providers.fold(emptyMap()) { acc, provider ->
            acc + provider.info
        }
}