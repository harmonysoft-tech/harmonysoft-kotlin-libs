package tech.harmonysoft.oss.http.server.mock.config

import javax.inject.Named
import tech.harmonysoft.oss.http.client.config.DefaultWebPortProvider

@Named
class DefaultWebPortProviderImpl(
    portHolder: PortHolder
) : DefaultWebPortProvider {

    override val port = portHolder.port
}