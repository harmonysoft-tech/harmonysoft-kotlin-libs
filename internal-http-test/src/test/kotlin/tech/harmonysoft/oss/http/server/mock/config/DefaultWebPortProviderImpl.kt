package tech.harmonysoft.oss.http.server.mock.config

import tech.harmonysoft.oss.http.client.cucumber.DefaultWebPortProvider
import javax.inject.Named

@Named
class DefaultWebPortProviderImpl(
    portHolder: PortHolder
) : DefaultWebPortProvider {

    override val port = portHolder.port
}